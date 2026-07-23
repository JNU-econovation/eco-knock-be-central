package jnu.econovation.ecoknockbecentral.group.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import jnu.econovation.ecoknockbecentral.common.converter.StringEncryptConverter;
import jnu.econovation.ecoknockbecentral.common.security.util.AES256Util;
import jnu.econovation.ecoknockbecentral.common.exception.constants.ErrorCode;
import jnu.econovation.ecoknockbecentral.group.dto.request.BrowseGroupsRequest;
import jnu.econovation.ecoknockbecentral.group.dto.request.CreateGroupRequest;
import jnu.econovation.ecoknockbecentral.group.exception.GroupClientException;
import jnu.econovation.ecoknockbecentral.group.model.entity.Group;
import jnu.econovation.ecoknockbecentral.group.model.entity.GroupApplication;
import jnu.econovation.ecoknockbecentral.group.model.entity.GroupMember;
import jnu.econovation.ecoknockbecentral.group.model.vo.GroupApplicationStatus;
import jnu.econovation.ecoknockbecentral.group.model.vo.GroupSort;
import jnu.econovation.ecoknockbecentral.group.model.vo.GroupType;
import jnu.econovation.ecoknockbecentral.group.model.vo.RecruitmentMode;
import jnu.econovation.ecoknockbecentral.group.model.vo.RecruitmentStatus;
import jnu.econovation.ecoknockbecentral.group.service.GroupService;
import jnu.econovation.ecoknockbecentral.group.service.GroupApplicationService;
import jnu.econovation.ecoknockbecentral.member.model.entity.Member;
import jnu.econovation.ecoknockbecentral.member.model.vo.ActiveStatus;
import jnu.econovation.ecoknockbecentral.member.model.vo.Cohort;
import jnu.econovation.ecoknockbecentral.member.repository.MemberRepository;
import jnu.econovation.ecoknockbecentral.member.service.MemberService;
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
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = GroupPostgresIntegrationTest.TestApplication.class)
@ActiveProfiles("dev")
@Transactional
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class GroupPostgresIntegrationTest {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final MemberRepository memberRepository;
    private final GroupService groupService;
    private final GroupApplicationRepository groupApplicationRepository;
    private final GroupApplicationService groupApplicationService;
    private final EntityManager entityManager;

    GroupPostgresIntegrationTest(
            GroupRepository groupRepository,
            GroupMemberRepository groupMemberRepository,
            GroupApplicationRepository groupApplicationRepository,
            MemberRepository memberRepository,
            GroupService groupService,
            GroupApplicationService groupApplicationService,
            EntityManager entityManager
    ) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.groupApplicationRepository = groupApplicationRepository;
        this.memberRepository = memberRepository;
        this.groupService = groupService;
        this.groupApplicationService = groupApplicationService;
        this.entityManager = entityManager;
    }

    @Test
    void serviceCreatesGroupAndLeaderAndRejectsDuplicatedName() {
        Member creator = saveMember(101L, "생성자");
        CreateGroupRequest request = new CreateGroupRequest(
                GroupType.STUDY, "통합생성", "소개", 5,
                RecruitmentMode.ALWAYS, null, null
        );

        Long groupId = groupService.create(creator.getId(), request).groupId();
        entityManager.flush();
        entityManager.clear();

        GroupMember leader = groupMemberRepository
                .findByGroupIdAndMemberId(groupId, creator.getId())
                .orElseThrow();
        assertThat(leader.getRole().name()).isEqualTo("LEADER");
        assertThat(groupRepository.findById(groupId)).isPresent();
        assertThatThrownBy(() -> groupService.create(creator.getId(), request))
                .isInstanceOfSatisfying(GroupClientException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.GROUP_NAME_DUPLICATED));
    }

    @Test
    void returnsMultipleMyGroupsByNameThenId() {
        Member member = saveMember(102L, "가입자");
        Group second = saveAlwaysGroup("나그룹", 5, member);
        Group first = saveAlwaysGroup("가그룹", 5, member);

        var result = groupService.getMyGroups(member.getId());

        assertThat(result).extracting(response -> response.groupId())
                .containsExactly(first.getId(), second.getId());
        assertThat(result).allMatch(response -> response.isLeader());
    }

    @Test
    void browseAggregatesMemberCountAndLeaderName() {
        Member leader = saveMember(103L, "집계리더");
        Member member = saveMember(104L, "집계멤버");
        Group group = saveAlwaysGroup("집계그룹", 5, leader);
        groupMemberRepository.saveAndFlush(GroupMember.member(group, member));
        entityManager.clear();

        var result = groupService.browse(new BrowseGroupsRequest(false, GroupSort.NAME_ASC));

        assertThat(result).filteredOn(response -> response.groupId().equals(group.getId()))
                .singleElement()
                .satisfies(response -> {
                    assertThat(response.currentMemberCount()).isEqualTo(2);
                    assertThat(response.leaderName()).isEqualTo("집계리더");
                });
    }

    @Test
    void excludeClosedKeepsUpcomingAndInclusivePeriodBoundaries() {
        Instant now = Instant.parse("2026-07-24T00:00:00Z");
        Member leader = saveMember(105L, "경계리더");
        Group full = saveAlwaysGroup("정원마감", 1, leader);
        Group ended = savePeriodGroup("기간마감", 3, now.minusSeconds(20), now.minusMillis(1), leader);
        Group upcoming = savePeriodGroup("모집예정", 3, now.plusSeconds(1), now.plusSeconds(20), leader);
        Group startsNow = savePeriodGroup("시작경계", 3, now, now.plusSeconds(20), leader);
        Group endsNow = savePeriodGroup("종료경계", 3, now.minusSeconds(20), now, leader);
        entityManager.flush();
        entityManager.clear();

        List<GroupBrowseRow> rows = groupRepository.findAllForBrowse(true, GroupSort.NAME_ASC, now);

        assertThat(rows).extracting(row -> row.group().getId())
                .contains(upcoming.getId(), startsNow.getId(), endsNow.getId())
                .doesNotContain(full.getId(), ended.getId());
        assertThat(upcoming.getRecruitmentStatus(1, now)).isEqualTo(RecruitmentStatus.UPCOMING);
        assertThat(startsNow.getRecruitmentStatus(1, now)).isEqualTo(RecruitmentStatus.RECRUITING);
        assertThat(endsNow.getRecruitmentStatus(1, now)).isEqualTo(RecruitmentStatus.RECRUITING);
    }

    @Test
    void appliesAllBrowseSortsAndDeadlineCategories() {
        Instant now = Instant.parse("2026-07-24T00:00:00Z");
        Member leader = saveMember(106L, "정렬리더");
        Group activeLater = savePeriodGroup("A-active", 5, now.minusSeconds(2), now.plusSeconds(20), leader);
        Group activeSoon = savePeriodGroup("B-active", 5, now.minusSeconds(2), now.plusSeconds(10), leader);
        Group upcomingLater = savePeriodGroup("C-upcoming", 5, now.plusSeconds(20), now.plusSeconds(40), leader);
        Group upcomingSoon = savePeriodGroup("D-upcoming", 5, now.plusSeconds(10), now.plusSeconds(40), leader);
        Group always = saveAlwaysGroup("E-always", 5, leader);
        Group closed = savePeriodGroup("F-closed", 5, now.minusSeconds(20), now.minusSeconds(10), leader);
        Group activeTieFirst = savePeriodGroup("G-tie-first", 5, now.minusSeconds(2), now.plusSeconds(15), leader);
        Group activeTieSecond = savePeriodGroup("H-tie-second", 5, now.minusSeconds(2), now.plusSeconds(15), leader);
        entityManager.flush();

        Instant sameCreatedAt = Instant.parse("2026-07-01T00:00:00Z");
        entityManager.createNativeQuery("UPDATE groups SET created_at = :createdAt WHERE id IN (:ids)")
                .setParameter("createdAt", sameCreatedAt)
                .setParameter("ids", List.of(
                        activeLater.getId(), activeSoon.getId(), upcomingLater.getId(),
                        upcomingSoon.getId(), always.getId(), closed.getId(),
                        activeTieFirst.getId(), activeTieSecond.getId()
                ))
                .executeUpdate();
        entityManager.clear();

        assertIds(
                groupRepository.findAllForBrowse(false, GroupSort.NAME_ASC, now),
                activeLater, activeSoon, upcomingLater, upcomingSoon, always, closed,
                activeTieFirst, activeTieSecond
        );
        assertIds(
                groupRepository.findAllForBrowse(false, GroupSort.NAME_DESC, now),
                activeTieSecond, activeTieFirst, closed, always, upcomingSoon, upcomingLater,
                activeSoon, activeLater
        );
        assertThat(groupRepository.findAllForBrowse(false, GroupSort.RECENT, now))
                .extracting(row -> row.group().getId())
                .containsSubsequence(
                        activeLater.getId(), activeSoon.getId(), upcomingLater.getId(),
                        upcomingSoon.getId(), always.getId(), closed.getId(),
                        activeTieFirst.getId(), activeTieSecond.getId()
                );
        assertIds(
                groupRepository.findAllForBrowse(false, GroupSort.DEADLINE_ASC, now),
                activeSoon, activeTieFirst, activeTieSecond, activeLater,
                upcomingSoon, upcomingLater, always, closed
        );
    }

    @Test
    void detailReturnsLeaderMembersCountAndRequesterRelationship() {
        Member leader = saveMember(107L, "상세리더");
        Member member = saveMember(108L, "상세멤버");
        Member outsider = saveMember(109L, "외부인");
        Group group = saveAlwaysGroup("상세그룹", 5, leader);
        groupMemberRepository.saveAndFlush(GroupMember.member(group, member));
        entityManager.clear();

        var leaderView = groupService.getDetail(group.getId(), leader.getId());
        var outsiderView = groupService.getDetail(group.getId(), outsider.getId());

        assertThat(leaderView.currentMemberCount()).isEqualTo(2);
        assertThat(leaderView.leaderName()).isEqualTo("상세리더");
        assertThat(leaderView.members()).extracting(response -> response.name())
                .containsExactlyInAnyOrder("상세리더", "상세멤버");
        assertThat(leaderView.isMember()).isTrue();
        assertThat(leaderView.isLeader()).isTrue();
        assertThat(outsiderView.isMember()).isFalse();
        assertThat(outsiderView.isLeader()).isFalse();
        assertThatThrownBy(() -> groupService.getDetail(Long.MAX_VALUE, outsider.getId()))
                .isInstanceOfSatisfying(GroupClientException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.GROUP_NOT_FOUND));
    }

    @Test
    void applicationLifecycleKeepsHistoryAndExposesOnlyPendingRows() {
        Member leader = saveMember(110L, "지원리더");
        Member applicant = saveMember(111L, "지원자");
        Group group = saveAlwaysGroup("지원통합", 3, leader);

        groupApplicationService.create(group.getId(), applicant.getId(), "  첫 지원  ");
        GroupApplication first = groupApplicationRepository
                .findAllByGroupIdAndStatusWithApplicant(
                        group.getId(),
                        GroupApplicationStatus.PENDING
                )
                .getFirst();
        groupApplicationService.reject(group.getId(), first.getId(), leader.getId());
        entityManager.flush();
        entityManager.clear();

        assertThat(groupApplicationService.getPendingApplications(
                group.getId(),
                leader.getId()
        )).isEmpty();
        assertThatThrownBy(() -> groupApplicationService.getPendingApplication(
                group.getId(),
                first.getId(),
                leader.getId()
        )).isInstanceOfSatisfying(GroupClientException.class, exception ->
                assertThat(exception.getErrorCode())
                        .isEqualTo(ErrorCode.GROUP_APPLICATION_NOT_FOUND));

        groupApplicationService.create(group.getId(), applicant.getId(), "재지원");
        List<GroupApplication> all = groupApplicationRepository.findAll();
        assertThat(all).filteredOn(application ->
                        application.getGroup().getId().equals(group.getId())
                                && application.getApplicant().getId().equals(applicant.getId()))
                .extracting(GroupApplication::getStatus)
                .containsExactlyInAnyOrder(
                        GroupApplicationStatus.REJECTED,
                        GroupApplicationStatus.PENDING
                );
    }

    @Test
    void acceptAddsMemberAndPreventsSecondProcessing() {
        Member leader = saveMember(112L, "수락리더");
        Member applicant = saveMember(113L, "수락지원자");
        Group group = saveAlwaysGroup("수락통합", 2, leader);
        groupApplicationService.create(group.getId(), applicant.getId(), "지원");
        GroupApplication application = groupApplicationRepository
                .findAllByGroupIdAndStatusWithApplicant(
                        group.getId(),
                        GroupApplicationStatus.PENDING
                )
                .getFirst();

        groupApplicationService.accept(group.getId(), application.getId(), leader.getId());
        entityManager.flush();
        entityManager.clear();

        assertThat(groupMemberRepository.existsByGroupIdAndMemberId(
                group.getId(),
                applicant.getId()
        )).isTrue();
        assertThatThrownBy(() -> groupApplicationService.accept(
                group.getId(),
                application.getId(),
                leader.getId()
        )).isInstanceOfSatisfying(GroupClientException.class, exception ->
                assertThat(exception.getErrorCode())
                        .isEqualTo(ErrorCode.GROUP_APPLICATION_ALREADY_PROCESSED));
    }

    private Member saveMember(Long ssoId, String name) {
        return memberRepository.saveAndFlush(Member.builder()
                .ssoMemberId(ssoId)
                .cohort(new Cohort(1))
                .name(name)
                .status(ActiveStatus.AM)
                .build());
    }

    private Group saveAlwaysGroup(String name, int capacity, Member leader) {
        Group group = groupRepository.saveAndFlush(Group.create(
                name, "소개", GroupType.STUDY, capacity,
                RecruitmentMode.ALWAYS, null, null
        ));
        groupMemberRepository.saveAndFlush(GroupMember.leader(group, leader));
        return group;
    }

    private Group savePeriodGroup(
            String name,
            int capacity,
            Instant startAt,
            Instant endAt,
            Member leader
    ) {
        Group group = groupRepository.saveAndFlush(Group.create(
                name, "소개", GroupType.STUDY, capacity,
                RecruitmentMode.PERIOD, startAt, endAt
        ));
        groupMemberRepository.saveAndFlush(GroupMember.leader(group, leader));
        return group;
    }

    private void assertIds(List<GroupBrowseRow> rows, Group... groups) {
        List<Long> expectedIds = java.util.Arrays.stream(groups).map(Group::getId).toList();
        assertThat(rows).extracting(row -> row.group().getId())
                .filteredOn(expectedIds::contains)
                .containsExactlyElementsOf(expectedIds);
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
