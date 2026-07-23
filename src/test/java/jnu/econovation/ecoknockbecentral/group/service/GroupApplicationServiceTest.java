package jnu.econovation.ecoknockbecentral.group.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import jnu.econovation.ecoknockbecentral.common.exception.constants.ErrorCode;
import jnu.econovation.ecoknockbecentral.group.exception.GroupClientException;
import jnu.econovation.ecoknockbecentral.group.model.entity.Group;
import jnu.econovation.ecoknockbecentral.group.model.entity.GroupApplication;
import jnu.econovation.ecoknockbecentral.group.model.entity.GroupMember;
import jnu.econovation.ecoknockbecentral.group.model.vo.GroupApplicationStatus;
import jnu.econovation.ecoknockbecentral.group.model.vo.GroupType;
import jnu.econovation.ecoknockbecentral.group.model.vo.RecruitmentMode;
import jnu.econovation.ecoknockbecentral.group.repository.GroupApplicationRepository;
import jnu.econovation.ecoknockbecentral.group.repository.GroupMemberRepository;
import jnu.econovation.ecoknockbecentral.group.repository.GroupRepository;
import jnu.econovation.ecoknockbecentral.member.model.entity.Member;
import jnu.econovation.ecoknockbecentral.member.model.vo.Role;
import jnu.econovation.ecoknockbecentral.member.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

class GroupApplicationServiceTest {

    private GroupRepository groupRepository;
    private GroupMemberRepository groupMemberRepository;
    private GroupApplicationRepository applicationRepository;
    private MemberService memberService;
    private GroupApplicationService service;

    @BeforeEach
    void setUp() {
        groupRepository = mock(GroupRepository.class);
        groupMemberRepository = mock(GroupMemberRepository.class);
        applicationRepository = mock(GroupApplicationRepository.class);
        memberService = mock(MemberService.class);
        service = new GroupApplicationService(
                groupRepository,
                groupMemberRepository,
                applicationRepository,
                memberService
        );
    }

    @Test
    void createsTrimmedPendingApplication() {
        Group group = alwaysGroup(2);
        Member applicant = member(2L, Role.USER, "지원자");
        when(groupRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(group));
        when(memberService.getEntityOrThrow(2L)).thenReturn(applicant);

        service.create(1L, 2L, "  지원합니다  ");

        ArgumentCaptor<GroupApplication> captor =
                ArgumentCaptor.forClass(GroupApplication.class);
        verify(applicationRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getContent()).isEqualTo("지원합니다");
        assertThat(captor.getValue().getStatus()).isEqualTo(GroupApplicationStatus.PENDING);
    }

    @Test
    void rejectsExistingMemberBeforePendingCheck() {
        Group group = alwaysGroup(2);
        Member applicant = member(2L, Role.USER, "지원자");
        when(groupRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(group));
        when(memberService.getEntityOrThrow(2L)).thenReturn(applicant);
        when(groupMemberRepository.existsByGroupIdAndMemberId(1L, 2L)).thenReturn(true);

        assertGroupError(
                () -> service.create(1L, 2L, "지원"),
                ErrorCode.GROUP_APPLICANT_ALREADY_MEMBER
        );
    }

    @Test
    void rejectsDuplicatedPendingApplication() {
        Group group = alwaysGroup(2);
        Member applicant = member(2L, Role.USER, "지원자");
        when(groupRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(group));
        when(memberService.getEntityOrThrow(2L)).thenReturn(applicant);
        when(applicationRepository.existsByGroupIdAndApplicantIdAndStatus(
                1L, 2L, GroupApplicationStatus.PENDING
        )).thenReturn(true);

        assertGroupError(
                () -> service.create(1L, 2L, "지원"),
                ErrorCode.GROUP_APPLICATION_ALREADY_PENDING
        );
    }

    @Test
    void translatesPendingUniqueConstraintRace() {
        Group group = alwaysGroup(2);
        Member applicant = member(2L, Role.USER, "지원자");
        when(groupRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(group));
        when(memberService.getEntityOrThrow(2L)).thenReturn(applicant);
        when(applicationRepository.saveAndFlush(any(GroupApplication.class)))
                .thenThrow(new DataIntegrityViolationException("pending unique"));

        assertGroupError(
                () -> service.create(1L, 2L, "지원"),
                ErrorCode.GROUP_APPLICATION_ALREADY_PENDING
        );
    }

    @Test
    void rejectsUpcomingEndedAndFullGroups() {
        Instant now = Instant.now();
        assertRecruitmentClosed(periodGroup(2, now.plusSeconds(60), now.plusSeconds(120)), 1);
        assertRecruitmentClosed(periodGroup(2, now.minusSeconds(120), now.minusSeconds(60)), 1);
        assertRecruitmentClosed(alwaysGroup(1), 1);
    }

    @Test
    void rejectsInvalidContentThroughServiceBoundary() {
        Group group = alwaysGroup(2);
        Member applicant = member(2L, Role.USER, "지원자");
        when(groupRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(group));
        when(memberService.getEntityOrThrow(2L)).thenReturn(applicant);

        assertGroupError(
                () -> service.create(1L, 2L, " ".repeat(3)),
                ErrorCode.GROUP_APPLICATION_CONTENT_INVALID
        );
    }

    @Test
    void returnsOnlyPendingApplicationsInRepositoryOrder() {
        Group group = alwaysGroup(3);
        Member member = member(2L, Role.USER, "그룹원");
        Member applicant = member(3L, Role.USER, "지원자");
        GroupApplication application = GroupApplication.pending(group, applicant, "지원");
        ReflectionTestUtils.setField(application, "id", 9L);
        ReflectionTestUtils.setField(application, "createdAt", Instant.parse("2026-07-24T00:00:00Z"));
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(memberService.getEntityOrThrow(2L)).thenReturn(member);
        when(groupMemberRepository.existsByGroupIdAndMemberId(1L, 2L)).thenReturn(true);
        when(applicationRepository.findAllByGroupIdAndStatusWithApplicant(
                1L, GroupApplicationStatus.PENDING
        )).thenReturn(List.of(application));

        var result = service.getPendingApplications(1L, 2L);

        assertThat(result).singleElement().satisfies(response -> {
            assertThat(response.applicationId()).isEqualTo(9L);
            assertThat(response.applicantMemberId()).isEqualTo(3L);
            assertThat(response.applicantName()).isEqualTo("지원자");
        });
    }

    @Test
    void hidesProcessedOrOtherGroupApplicationFromDetail() {
        Group group = alwaysGroup(3);
        Member member = member(2L, Role.USER, "그룹원");
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(memberService.getEntityOrThrow(2L)).thenReturn(member);
        when(groupMemberRepository.existsByGroupIdAndMemberId(1L, 2L)).thenReturn(true);
        when(applicationRepository.findByIdAndGroupIdAndStatusWithApplicant(
                9L, 1L, GroupApplicationStatus.PENDING
        )).thenReturn(Optional.empty());

        assertGroupError(
                () -> service.getPendingApplication(1L, 9L, 2L),
                ErrorCode.GROUP_APPLICATION_NOT_FOUND
        );
    }

    @Test
    void deniesApplicationReadToNonMemberButAllowsAdminWithoutMembership() {
        Group group = alwaysGroup(3);
        Member outsider = member(2L, Role.USER, "외부인");
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(memberService.getEntityOrThrow(2L)).thenReturn(outsider);

        assertGroupError(
                () -> service.getPendingApplications(1L, 2L),
                ErrorCode.GROUP_ACCESS_DENIED
        );

        Member admin = member(3L, Role.ADMIN, "관리자");
        when(memberService.getEntityOrThrow(3L)).thenReturn(admin);
        when(applicationRepository.findAllByGroupIdAndStatusWithApplicant(
                1L, GroupApplicationStatus.PENDING
        )).thenReturn(List.of());
        assertThat(service.getPendingApplications(1L, 3L)).isEmpty();
    }

    @Test
    void acceptsWithGroupThenApplicationLockAndAddsMember() {
        Group group = alwaysGroup(2);
        Member leader = member(1L, Role.USER, "리더");
        Member applicant = member(2L, Role.USER, "지원자");
        GroupApplication application = GroupApplication.pending(group, applicant, "지원");
        when(groupRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(group));
        when(memberService.getEntityOrThrow(1L)).thenReturn(leader);
        when(groupMemberRepository.findByGroupIdAndMemberId(1L, 1L))
                .thenReturn(Optional.of(GroupMember.leader(group, leader)));
        when(applicationRepository.findByIdAndGroupIdForUpdate(9L, 1L))
                .thenReturn(Optional.of(application));
        when(groupMemberRepository.countByGroupId(1L)).thenReturn(1L);

        service.accept(1L, 9L, 1L);

        var order = inOrder(groupRepository, applicationRepository);
        order.verify(groupRepository).findByIdForUpdate(1L);
        order.verify(applicationRepository).findByIdAndGroupIdForUpdate(9L, 1L);
        verify(groupMemberRepository).saveAndFlush(any(GroupMember.class));
        assertThat(application.getStatus()).isEqualTo(GroupApplicationStatus.ACCEPTED);
    }

    @Test
    void rejectsProcessingWhenCapacityWasFilledUnderLock() {
        Group group = alwaysGroup(1);
        Member admin = member(1L, Role.ADMIN, "관리자");
        Member applicant = member(2L, Role.USER, "지원자");
        GroupApplication application = GroupApplication.pending(group, applicant, "지원");
        when(groupRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(group));
        when(memberService.getEntityOrThrow(1L)).thenReturn(admin);
        when(applicationRepository.findByIdAndGroupIdForUpdate(9L, 1L))
                .thenReturn(Optional.of(application));
        when(groupMemberRepository.countByGroupId(1L)).thenReturn(1L);

        assertGroupError(
                () -> service.accept(1L, 9L, 1L),
                ErrorCode.GROUP_CAPACITY_REACHED
        );
    }

    @Test
    void rejectsAcceptanceWhenApplicantAlreadyBecameMember() {
        Group group = alwaysGroup(3);
        Member admin = member(1L, Role.ADMIN, "관리자");
        Member applicant = member(2L, Role.USER, "지원자");
        GroupApplication application = GroupApplication.pending(group, applicant, "지원");
        when(groupRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(group));
        when(memberService.getEntityOrThrow(1L)).thenReturn(admin);
        when(applicationRepository.findByIdAndGroupIdForUpdate(9L, 1L))
                .thenReturn(Optional.of(application));
        when(groupMemberRepository.countByGroupId(1L)).thenReturn(1L);
        when(groupMemberRepository.existsByGroupIdAndMemberId(1L, 2L)).thenReturn(true);

        assertGroupError(
                () -> service.accept(1L, 9L, 1L),
                ErrorCode.GROUP_MEMBER_ALREADY_EXISTS
        );
    }

    @Test
    void rejectsAlreadyProcessedApplication() {
        Group group = alwaysGroup(2);
        Member admin = member(1L, Role.ADMIN, "관리자");
        Member applicant = member(2L, Role.USER, "지원자");
        GroupApplication application = GroupApplication.pending(group, applicant, "지원");
        application.reject();
        when(groupRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(group));
        when(memberService.getEntityOrThrow(1L)).thenReturn(admin);
        when(applicationRepository.findByIdAndGroupIdForUpdate(9L, 1L))
                .thenReturn(Optional.of(application));

        assertGroupError(
                () -> service.reject(1L, 9L, 1L),
                ErrorCode.GROUP_APPLICATION_ALREADY_PROCESSED
        );
    }

    @Test
    void rejectsWithGroupThenApplicationLock() {
        Group group = alwaysGroup(2);
        Member admin = member(1L, Role.ADMIN, "관리자");
        Member applicant = member(2L, Role.USER, "지원자");
        GroupApplication application = GroupApplication.pending(group, applicant, "지원");
        when(groupRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(group));
        when(memberService.getEntityOrThrow(1L)).thenReturn(admin);
        when(applicationRepository.findByIdAndGroupIdForUpdate(9L, 1L))
                .thenReturn(Optional.of(application));

        service.reject(1L, 9L, 1L);

        var order = inOrder(groupRepository, applicationRepository);
        order.verify(groupRepository).findByIdForUpdate(1L);
        order.verify(applicationRepository).findByIdAndGroupIdForUpdate(9L, 1L);
        assertThat(application.getStatus()).isEqualTo(GroupApplicationStatus.REJECTED);
    }

    @Test
    void rejectsNonLeaderProcessingButAllowsAdminWithoutMembership() {
        Group group = alwaysGroup(2);
        Member ordinary = member(1L, Role.USER, "일반");
        when(groupRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(group));
        when(memberService.getEntityOrThrow(1L)).thenReturn(ordinary);
        when(groupMemberRepository.findByGroupIdAndMemberId(1L, 1L))
                .thenReturn(Optional.of(GroupMember.member(group, ordinary)));

        assertGroupError(
                () -> service.reject(1L, 9L, 1L),
                ErrorCode.GROUP_LEADER_PERMISSION_REQUIRED
        );
    }

    private void assertRecruitmentClosed(Group group, long memberCount) {
        Member applicant = member(2L, Role.USER, "지원자");
        when(groupRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(group));
        when(memberService.getEntityOrThrow(2L)).thenReturn(applicant);
        when(groupMemberRepository.countByGroupId(1L)).thenReturn(memberCount);
        assertGroupError(
                () -> service.create(1L, 2L, "지원"),
                ErrorCode.GROUP_RECRUITMENT_CLOSED
        );
    }

    private Group alwaysGroup(int capacity) {
        Group group = Group.create(
                "그룹", "소개", GroupType.STUDY, capacity,
                RecruitmentMode.ALWAYS, null, null
        );
        ReflectionTestUtils.setField(group, "id", 1L);
        return group;
    }

    private Group periodGroup(int capacity, Instant start, Instant end) {
        Group group = Group.create(
                "그룹", "소개", GroupType.STUDY, capacity,
                RecruitmentMode.PERIOD, start, end
        );
        ReflectionTestUtils.setField(group, "id", 1L);
        return group;
    }

    private Member member(Long id, Role role, String name) {
        Member member = mock(Member.class);
        when(member.getId()).thenReturn(id);
        when(member.getRole()).thenReturn(role);
        when(member.getName()).thenReturn(name);
        return member;
    }

    private void assertGroupError(Runnable action, ErrorCode errorCode) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(GroupClientException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(errorCode));
    }
}
