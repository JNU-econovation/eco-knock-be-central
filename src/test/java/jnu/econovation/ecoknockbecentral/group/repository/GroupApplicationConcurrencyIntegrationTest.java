package jnu.econovation.ecoknockbecentral.group.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import jnu.econovation.ecoknockbecentral.common.converter.StringEncryptConverter;
import jnu.econovation.ecoknockbecentral.common.exception.constants.ErrorCode;
import jnu.econovation.ecoknockbecentral.common.security.util.AES256Util;
import jnu.econovation.ecoknockbecentral.group.dto.request.CreateGroupRequest;
import jnu.econovation.ecoknockbecentral.group.exception.GroupClientException;
import jnu.econovation.ecoknockbecentral.group.model.entity.GroupApplication;
import jnu.econovation.ecoknockbecentral.group.model.vo.GroupApplicationStatus;
import jnu.econovation.ecoknockbecentral.group.model.vo.GroupType;
import jnu.econovation.ecoknockbecentral.group.model.vo.RecruitmentMode;
import jnu.econovation.ecoknockbecentral.group.service.GroupApplicationService;
import jnu.econovation.ecoknockbecentral.group.service.GroupService;
import jnu.econovation.ecoknockbecentral.member.model.entity.Member;
import jnu.econovation.ecoknockbecentral.member.model.vo.ActiveStatus;
import jnu.econovation.ecoknockbecentral.member.model.vo.Cohort;
import jnu.econovation.ecoknockbecentral.member.repository.MemberRepository;
import jnu.econovation.ecoknockbecentral.member.service.MemberService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest(classes = GroupApplicationConcurrencyIntegrationTest.TestApplication.class)
@ActiveProfiles("dev")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class GroupApplicationConcurrencyIntegrationTest {

    private static final String GROUP_NAME = "동시수락통합";
    private static final List<Long> SSO_IDS = List.of(901L, 902L, 903L);

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupApplicationRepository groupApplicationRepository;
    private final MemberRepository memberRepository;
    private final GroupService groupService;
    private final GroupApplicationService groupApplicationService;
    private final TransactionTemplate transactionTemplate;

    GroupApplicationConcurrencyIntegrationTest(
            GroupRepository groupRepository,
            GroupMemberRepository groupMemberRepository,
            GroupApplicationRepository groupApplicationRepository,
            MemberRepository memberRepository,
            GroupService groupService,
            GroupApplicationService groupApplicationService,
            PlatformTransactionManager transactionManager
    ) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.groupApplicationRepository = groupApplicationRepository;
        this.memberRepository = memberRepository;
        this.groupService = groupService;
        this.groupApplicationService = groupApplicationService;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @AfterEach
    @BeforeEach
    void cleanUp() {
        transactionTemplate.executeWithoutResult(status -> {
            groupRepository.findAll().stream()
                    .filter(group -> GROUP_NAME.equals(group.getName()))
                    .forEach(groupRepository::delete);
            memberRepository.findAll().stream()
                    .filter(member -> SSO_IDS.contains(member.getSsoMemberId()))
                    .forEach(memberRepository::delete);
        });
    }

    @Test
    void serializesLastSeatAcceptanceAndNeverExceedsCapacity() throws Exception {
        Fixture fixture = transactionTemplate.execute(status -> {
            Member leader = saveMember(901L, "동시리더");
            Member first = saveMember(902L, "동시지원자1");
            Member second = saveMember(903L, "동시지원자2");
            Long groupId = groupService.create(
                    leader.getId(),
                    new CreateGroupRequest(
                            GroupType.STUDY,
                            GROUP_NAME,
                            "소개",
                            2,
                            RecruitmentMode.ALWAYS,
                            null,
                            null
                    )
            ).groupId();
            groupApplicationService.create(groupId, first.getId(), "첫 지원");
            groupApplicationService.create(groupId, second.getId(), "둘 지원");
            List<GroupApplication> applications =
                    groupApplicationRepository.findAllByGroupIdAndStatusWithApplicant(
                            groupId,
                            GroupApplicationStatus.PENDING
                    );
            return new Fixture(
                    groupId,
                    leader.getId(),
                    applications.get(0).getId(),
                    applications.get(1).getId()
            );
        });

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            Future<ErrorCode> first = executor.submit(
                    () -> acceptAfterSignal(fixture, fixture.firstApplicationId(), ready, start)
            );
            Future<ErrorCode> second = executor.submit(
                    () -> acceptAfterSignal(fixture, fixture.secondApplicationId(), ready, start)
            );
            ready.await();
            start.countDown();

            assertThat(java.util.Arrays.asList(first.get(), second.get()))
                    .containsExactlyInAnyOrder(null, ErrorCode.GROUP_CAPACITY_REACHED);
        }

        transactionTemplate.executeWithoutResult(status -> {
            assertThat(groupMemberRepository.countByGroupId(fixture.groupId())).isEqualTo(2);
            assertThat(groupApplicationRepository.findAllByGroupIdAndStatusWithApplicant(
                    fixture.groupId(),
                    GroupApplicationStatus.ACCEPTED
            )).hasSize(1);
        });
    }

    private ErrorCode acceptAfterSignal(
            Fixture fixture,
            Long applicationId,
            CountDownLatch ready,
            CountDownLatch start
    ) throws InterruptedException {
        ready.countDown();
        start.await();
        try {
            groupApplicationService.accept(
                    fixture.groupId(),
                    applicationId,
                    fixture.leaderId()
            );
            return null;
        } catch (GroupClientException exception) {
            return exception.getErrorCode();
        }
    }

    private Member saveMember(Long ssoId, String name) {
        return memberRepository.saveAndFlush(Member.builder()
                .ssoMemberId(ssoId)
                .cohort(new Cohort(1))
                .name(name)
                .status(ActiveStatus.AM)
                .build());
    }

    private record Fixture(
            Long groupId,
            Long leaderId,
            Long firstApplicationId,
            Long secondApplicationId
    ) {
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EnableJpaAuditing
    @EntityScan(basePackages = "jnu.econovation.ecoknockbecentral")
    @EnableJpaRepositories(basePackages = {
            "jnu.econovation.ecoknockbecentral.group.repository",
            "jnu.econovation.ecoknockbecentral.member.repository"
    })
    @Import({
            AES256Util.class,
            StringEncryptConverter.class,
            GroupService.class,
            GroupApplicationService.class,
            MemberService.class
    })
    static class TestApplication {
    }
}
