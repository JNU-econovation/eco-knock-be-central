package jnu.econovation.ecoknockbecentral.group.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import jnu.econovation.ecoknockbecentral.common.exception.constants.ErrorCode;
import jnu.econovation.ecoknockbecentral.group.dto.request.BrowseGroupsRequest;
import jnu.econovation.ecoknockbecentral.group.dto.request.CreateGroupRequest;
import jnu.econovation.ecoknockbecentral.group.dto.request.UpdateGroupDetailRequest;
import jnu.econovation.ecoknockbecentral.group.dto.request.UpdateGroupNameRequest;
import jnu.econovation.ecoknockbecentral.group.dto.request.UpdateGroupRecruitmentRequest;
import jnu.econovation.ecoknockbecentral.group.exception.GroupClientException;
import jnu.econovation.ecoknockbecentral.group.model.entity.Group;
import jnu.econovation.ecoknockbecentral.group.model.entity.GroupMember;
import jnu.econovation.ecoknockbecentral.group.model.vo.GroupMemberRole;
import jnu.econovation.ecoknockbecentral.group.model.vo.GroupApplicationStatus;
import jnu.econovation.ecoknockbecentral.group.model.vo.GroupSort;
import jnu.econovation.ecoknockbecentral.group.model.vo.GroupType;
import jnu.econovation.ecoknockbecentral.group.model.vo.MyGroupApplicationStatus;
import jnu.econovation.ecoknockbecentral.group.model.vo.RecruitmentMode;
import jnu.econovation.ecoknockbecentral.group.model.vo.RecruitmentStatus;
import jnu.econovation.ecoknockbecentral.group.repository.GroupBrowseRow;
import jnu.econovation.ecoknockbecentral.group.repository.GroupApplicationRepository;
import jnu.econovation.ecoknockbecentral.group.repository.GroupMemberRepository;
import jnu.econovation.ecoknockbecentral.group.repository.GroupRepository;
import jnu.econovation.ecoknockbecentral.member.model.entity.Member;
import jnu.econovation.ecoknockbecentral.member.model.vo.Role;
import jnu.econovation.ecoknockbecentral.member.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

class GroupServiceTest {

    private GroupRepository groupRepository;
    private GroupMemberRepository groupMemberRepository;
    private GroupApplicationRepository groupApplicationRepository;
    private MemberService memberService;
    private GroupService service;

    @BeforeEach
    void setUp() {
        groupRepository = mock(GroupRepository.class);
        groupMemberRepository = mock(GroupMemberRepository.class);
        groupApplicationRepository = mock(GroupApplicationRepository.class);
        memberService = mock(MemberService.class);
        service = new GroupService(
                groupRepository,
                groupMemberRepository,
                groupApplicationRepository,
                memberService
        );
    }

    @Test
    void createsGroupAndLeaderMembershipAtomically() {
        Member creator = mock(Member.class);
        when(memberService.getEntityOrThrow(7L)).thenReturn(creator);
        when(groupRepository.saveAndFlush(any(Group.class))).thenAnswer(invocation -> {
            Group group = invocation.getArgument(0);
            ReflectionTestUtils.setField(group, "id", 31L);
            return group;
        });

        var response = service.create(7L, alwaysRequest("  백엔드  "));

        assertThat(response.groupId()).isEqualTo(31L);
        ArgumentCaptor<Group> groupCaptor = ArgumentCaptor.forClass(Group.class);
        verify(groupRepository).saveAndFlush(groupCaptor.capture());
        assertThat(groupCaptor.getValue().getName()).isEqualTo("백엔드");
        ArgumentCaptor<GroupMember> memberCaptor = ArgumentCaptor.forClass(GroupMember.class);
        verify(groupMemberRepository).save(memberCaptor.capture());
        assertThat(memberCaptor.getValue().getRole()).isEqualTo(GroupMemberRole.LEADER);
        assertThat(memberCaptor.getValue().getMember()).isSameAs(creator);
    }

    @Test
    void rejectsDuplicatedNameBeforeCrossDomainLookup() {
        when(groupRepository.existsByName("백엔드")).thenReturn(true);

        assertThatThrownBy(() -> service.create(7L, alwaysRequest("백엔드")))
                .isInstanceOfSatisfying(GroupClientException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.GROUP_NAME_DUPLICATED));
    }

    @Test
    void rejectsInvalidPeriodWithGroupError() {
        ZonedDateTime start = ZonedDateTime.of(2026, 7, 25, 0, 0, 0, 0, ZoneOffset.UTC);
        ZonedDateTime end = start.minusSeconds(1);
        CreateGroupRequest request = new CreateGroupRequest(
                GroupType.STUDY, "백엔드", "소개", 10,
                RecruitmentMode.PERIOD, start, end
        );

        assertThatThrownBy(() -> service.create(7L, request))
                .isInstanceOfSatisfying(GroupClientException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.GROUP_RECRUITMENT_PERIOD_INVALID));
    }

    @Test
    void rejectsInvalidCapacityWithGroupError() {
        CreateGroupRequest request = new CreateGroupRequest(
                GroupType.STUDY, "백엔드", "소개", 51,
                RecruitmentMode.ALWAYS, null, null
        );

        assertThatThrownBy(() -> service.create(7L, request))
                .isInstanceOfSatisfying(GroupClientException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.GROUP_CAPACITY_INVALID));
    }

    @Test
    void mapsBrowseRowsWithCalculatedStatus() {
        Group group = Group.create(
                "상시", "소개", GroupType.STUDY, 2,
                RecruitmentMode.ALWAYS, null, null
        );
        ReflectionTestUtils.setField(group, "id", 3L);
        when(groupRepository.findAllForBrowse(
                eq(false),
                eq(GroupSort.NAME_ASC),
                any(Instant.class),
                eq(7L)
        ))
                .thenReturn(List.of(new GroupBrowseRow(group, 2, "리더", true, true)));

        var result = service.browse(
                new BrowseGroupsRequest(false, GroupSort.NAME_ASC),
                7L
        );

        assertThat(result).singleElement().satisfies(response -> {
            assertThat(response.currentMemberCount()).isEqualTo(2);
            assertThat(response.leaderName()).isEqualTo("리더");
            assertThat(response.recruitmentStatus()).isEqualTo(RecruitmentStatus.CLOSED);
            assertThat(response.type()).isEqualTo(GroupType.STUDY);
            assertThat(response.isMember()).isTrue();
            assertThat(response.isLeader()).isTrue();
        });
    }

    @Test
    void returnsDetailRelationshipAndInclusiveEndBoundary() {
        Instant end = Instant.now().plusSeconds(30);
        Group group = Group.create(
                "기간", "소개", GroupType.DEPARTMENT, 3,
                RecruitmentMode.PERIOD, end.minusSeconds(60), end
        );
        ReflectionTestUtils.setField(group, "id", 9L);
        Member leaderMember = mock(Member.class);
        when(leaderMember.getId()).thenReturn(7L);
        when(leaderMember.getName()).thenReturn("리더");
        GroupMember leader = GroupMember.leader(group, leaderMember);
        when(groupRepository.findById(9L)).thenReturn(Optional.of(group));
        when(groupMemberRepository.findAllByGroupIdWithMember(9L)).thenReturn(List.of(leader));

        var response = service.getDetail(9L, 7L, Role.USER);

        assertThat(response.isMember()).isTrue();
        assertThat(response.isLeader()).isTrue();
        assertThat(response.members()).containsExactly(new jnu.econovation.ecoknockbecentral.group.dto.response.GroupMemberResponse("리더"));
        assertThat(response.recruitmentStatus()).isEqualTo(RecruitmentStatus.RECRUITING);
        assertThat(response.permissions().canEditGroup()).isTrue();
        assertThat(response.permissions().canApply()).isFalse();
    }

    @Test
    void rejectsMissingDetail() {
        when(groupRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getDetail(404L, 7L, Role.USER))
                .isInstanceOfSatisfying(GroupClientException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.GROUP_NOT_FOUND));
    }

    @Test
    void calculatesApplicationStateAndPermissionsForEveryRequesterKind() {
        Group group = group("권한그룹", 5);
        Member leaderMember = member(7L, "리더", Role.USER);
        Member ordinaryMember = member(8L, "멤버", Role.USER);
        when(groupRepository.findById(9L)).thenReturn(Optional.of(group));
        when(groupMemberRepository.findAllByGroupIdWithMember(9L)).thenReturn(List.of(
                GroupMember.leader(group, leaderMember),
                GroupMember.member(group, ordinaryMember)
        ));
        when(groupApplicationRepository.existsByGroupIdAndApplicantIdAndStatus(
                9L, 10L, GroupApplicationStatus.PENDING
        )).thenReturn(true);

        var leader = service.getDetail(9L, 7L, Role.USER);
        var member = service.getDetail(9L, 8L, Role.USER);
        var pendingApplicant = service.getDetail(9L, 10L, Role.USER);
        var admin = service.getDetail(9L, 99L, Role.ADMIN);
        var guest = service.getDetail(9L, 100L, Role.GUEST);

        assertThat(leader.permissions()).satisfies(permissions -> {
            assertThat(permissions.canViewSettings()).isTrue();
            assertThat(permissions.canEditGroup()).isTrue();
            assertThat(permissions.canManageMembers()).isTrue();
            assertThat(permissions.canViewApplications()).isTrue();
            assertThat(permissions.canReviewApplications()).isTrue();
            assertThat(permissions.canDeleteGroup()).isTrue();
            assertThat(permissions.canApply()).isFalse();
        });
        assertThat(member.permissions()).satisfies(permissions -> {
            assertThat(permissions.canViewSettings()).isTrue();
            assertThat(permissions.canEditGroup()).isFalse();
            assertThat(permissions.canViewApplications()).isTrue();
            assertThat(permissions.canReviewApplications()).isFalse();
            assertThat(permissions.canApply()).isFalse();
        });
        assertThat(pendingApplicant.myApplicationStatus())
                .isEqualTo(MyGroupApplicationStatus.PENDING);
        assertThat(pendingApplicant.permissions().canApply()).isFalse();
        assertThat(admin.permissions().canEditGroup()).isTrue();
        assertThat(admin.permissions().canApply()).isTrue();
        assertThat(guest.permissions()).satisfies(permissions -> {
            assertThat(permissions.canViewSettings()).isFalse();
            assertThat(permissions.canEditGroup()).isFalse();
            assertThat(permissions.canViewApplications()).isFalse();
            assertThat(permissions.canApply()).isFalse();
        });
    }

    @Test
    void disallowsApplicationWhenGroupIsUpcomingOrFull() {
        Member leaderMember = member(7L, "리더", Role.USER);
        Group full = group("정원마감", 1);
        Instant now = Instant.now();
        Group upcoming = Group.create(
                "모집예정",
                "소개",
                GroupType.STUDY,
                2,
                RecruitmentMode.PERIOD,
                now.plusSeconds(60),
                now.plusSeconds(120)
        );
        ReflectionTestUtils.setField(upcoming, "id", 10L);
        when(groupRepository.findById(9L)).thenReturn(Optional.of(full));
        when(groupRepository.findById(10L)).thenReturn(Optional.of(upcoming));
        when(groupMemberRepository.findAllByGroupIdWithMember(9L))
                .thenReturn(List.of(GroupMember.leader(full, leaderMember)));
        when(groupMemberRepository.findAllByGroupIdWithMember(10L))
                .thenReturn(List.of(GroupMember.leader(upcoming, leaderMember)));

        assertThat(service.getDetail(9L, 20L, Role.USER).permissions().canApply()).isFalse();
        assertThat(service.getDetail(10L, 20L, Role.USER).permissions().canApply()).isFalse();
    }

    @Test
    void exposesMemberIdentityOnlyToGroupMemberOrAdmin() {
        Group group = group("조회그룹", 5);
        Member first = member(7L, "동명이인", Role.USER);
        Member second = member(8L, "동명이인", Role.USER);
        when(groupRepository.findById(9L)).thenReturn(Optional.of(group));
        when(groupMemberRepository.existsByGroupIdAndMemberId(9L, 7L)).thenReturn(true);
        when(groupMemberRepository.findAllByGroupIdWithMember(9L)).thenReturn(List.of(
                GroupMember.leader(group, first),
                GroupMember.member(group, second)
        ));

        assertThat(service.getMembers(9L, 7L, Role.USER))
                .extracting(response -> response.memberId())
                .containsExactly(7L, 8L);
        assertThat(service.getMembers(9L, 99L, Role.ADMIN))
                .extracting(response -> response.name())
                .containsOnly("동명이인");
        assertGroupError(
                () -> service.getMembers(9L, 10L, Role.USER),
                ErrorCode.GROUP_ACCESS_DENIED
        );
        assertGroupError(
                () -> service.getMembers(9L, 11L, Role.GUEST),
                ErrorCode.GROUP_ACCESS_DENIED
        );
    }

    @Test
    void updatesNameAndAllowsCurrentNameAsNoOp() {
        Group group = group("기존이름", 5);
        Member leader = member(7L, "리더", Role.USER);
        GroupMember membership = GroupMember.leader(group, leader);
        when(groupRepository.findByIdForUpdate(9L)).thenReturn(Optional.of(group));
        when(memberService.getEntityOrThrow(7L)).thenReturn(leader);
        when(groupMemberRepository.findByGroupIdAndMemberId(9L, 7L))
                .thenReturn(Optional.of(membership));

        service.updateName(9L, 7L, new UpdateGroupNameRequest(" 기존이름 "));

        verify(groupRepository, never()).existsByNameAndIdNot(any(), any());
        assertThat(group.getName()).isEqualTo("기존이름");
    }

    @Test
    void rejectsDuplicatedUpdatedNameAndMapsDatabaseRace() {
        Group group = group("기존이름", 5);
        Member leader = member(7L, "리더", Role.USER);
        arrangeLeader(group, leader);
        when(groupRepository.existsByNameAndIdNot("중복이름", 9L)).thenReturn(true);

        assertGroupError(
                () -> service.updateName(9L, 7L, new UpdateGroupNameRequest("중복이름")),
                ErrorCode.GROUP_NAME_DUPLICATED
        );
        assertThat(group.getName()).isEqualTo("기존이름");

        when(groupRepository.existsByNameAndIdNot("경합이름", 9L)).thenReturn(false);
        org.mockito.Mockito.doThrow(new DataIntegrityViolationException("unique"))
                .when(groupRepository).flush();
        assertGroupError(
                () -> service.updateName(9L, 7L, new UpdateGroupNameRequest("경합이름")),
                ErrorCode.GROUP_NAME_DUPLICATED
        );
    }

    @Test
    void updatesDetailsAndPreservesStateWhenCapacityIsBelowCurrentCount() {
        Group group = group("그룹", 5);
        Member leader = member(7L, "리더", Role.USER);
        arrangeLeader(group, leader);
        when(groupMemberRepository.countByGroupId(9L)).thenReturn(3L);

        assertGroupError(
                () -> service.updateDetails(
                        9L,
                        7L,
                        new UpdateGroupDetailRequest(GroupType.DEPARTMENT, "새 소개", 2)
                ),
                ErrorCode.GROUP_CAPACITY_INVALID
        );

        assertThat(group.getType()).isEqualTo(GroupType.STUDY);
        assertThat(group.getDescription()).isEqualTo("소개");
        assertThat(group.getCapacity()).isEqualTo(5);
    }

    @Test
    void rejectsInvalidRecruitmentWithoutMutatingExistingSetting() {
        Group group = group("그룹", 5);
        Member leader = member(7L, "리더", Role.USER);
        arrangeLeader(group, leader);
        ZonedDateTime start = ZonedDateTime.now(ZoneOffset.UTC);

        assertGroupError(
                () -> service.updateRecruitment(
                        9L,
                        7L,
                        new UpdateGroupRecruitmentRequest(
                                RecruitmentMode.PERIOD,
                                start,
                                start.minusSeconds(1)
                        )
                ),
                ErrorCode.GROUP_RECRUITMENT_PERIOD_INVALID
        );

        assertThat(group.getRecruitmentMode()).isEqualTo(RecruitmentMode.ALWAYS);
        assertThat(group.getRecruitmentStartAt()).isNull();
        assertThat(group.getRecruitmentEndAt()).isNull();
    }

    @Test
    void adminCanManageWithoutMembershipAndManagementListIsOrdered() {
        Group group = group("그룹", 5);
        Member admin = member(99L, "관리자", Role.ADMIN);
        Member leader = member(7L, "리더", Role.USER);
        Member memberA = member(8L, "가", Role.USER);
        Member memberB = member(9L, "나", Role.USER);
        when(groupRepository.findById(9L)).thenReturn(Optional.of(group));
        when(memberService.getEntityOrThrow(99L)).thenReturn(admin);
        when(groupMemberRepository.findAllForManagement(9L)).thenReturn(List.of(
                GroupMember.member(group, memberB),
                GroupMember.member(group, memberA),
                GroupMember.leader(group, leader)
        ));

        var result = service.getMembersForManagement(9L, 99L);

        assertThat(result).extracting(response -> response.memberId())
                .containsExactly(7L, 8L, 9L);
        assertThat(result.getFirst().leader()).isTrue();
    }

    @Test
    void ordinaryMemberCannotManage() {
        Group group = group("그룹", 5);
        Member member = member(8L, "멤버", Role.USER);
        when(groupRepository.findById(9L)).thenReturn(Optional.of(group));
        when(memberService.getEntityOrThrow(8L)).thenReturn(member);
        when(groupMemberRepository.findByGroupIdAndMemberId(9L, 8L))
                .thenReturn(Optional.of(GroupMember.member(group, member)));

        assertGroupError(
                () -> service.getMembersForManagement(9L, 8L),
                ErrorCode.GROUP_LEADER_PERMISSION_REQUIRED
        );
    }

    @Test
    void removesOrdinaryMemberButNeverLeader() {
        Group group = group("그룹", 5);
        Member admin = member(99L, "관리자", Role.ADMIN);
        Member target = member(8L, "멤버", Role.USER);
        when(groupRepository.findByIdForUpdate(9L)).thenReturn(Optional.of(group));
        when(memberService.getEntityOrThrow(99L)).thenReturn(admin);
        GroupMember ordinary = GroupMember.member(group, target);
        when(groupMemberRepository.findByGroupIdAndMemberIdForUpdate(9L, 8L))
                .thenReturn(Optional.of(ordinary));

        service.removeMember(9L, 99L, 8L);
        verify(groupMemberRepository).delete(ordinary);

        when(groupMemberRepository.findByGroupIdAndMemberIdForUpdate(9L, 8L))
                .thenReturn(Optional.of(GroupMember.leader(group, target)));
        assertGroupError(
                () -> service.removeMember(9L, 99L, 8L),
                ErrorCode.GROUP_LEADER_CANNOT_BE_REMOVED
        );

        when(groupMemberRepository.findByGroupIdAndMemberIdForUpdate(9L, 404L))
                .thenReturn(Optional.empty());
        assertGroupError(
                () -> service.removeMember(9L, 99L, 404L),
                ErrorCode.GROUP_MEMBER_NOT_FOUND
        );
    }

    @Test
    void transfersLeaderWithDemoteFlushPromoteFlushOrder() {
        Group group = group("그룹", 5);
        Member admin = member(99L, "관리자", Role.ADMIN);
        Member oldMember = member(7L, "기존", Role.USER);
        Member newMember = member(8L, "신규", Role.USER);
        GroupMember oldLeader = GroupMember.leader(group, oldMember);
        GroupMember newLeader = GroupMember.member(group, newMember);
        when(groupRepository.findByIdForUpdate(9L)).thenReturn(Optional.of(group));
        when(memberService.getEntityOrThrow(99L)).thenReturn(admin);
        when(groupMemberRepository.findLeaderByGroupIdForUpdate(9L))
                .thenReturn(Optional.of(oldLeader));
        when(groupMemberRepository.findByGroupIdAndMemberIdForUpdate(9L, 8L))
                .thenReturn(Optional.of(newLeader));

        service.changeLeader(9L, 99L, 8L);

        assertThat(oldLeader.getRole()).isEqualTo(GroupMemberRole.MEMBER);
        assertThat(newLeader.getRole()).isEqualTo(GroupMemberRole.LEADER);
        verify(groupMemberRepository, org.mockito.Mockito.times(2)).flush();
        InOrder locks = inOrder(groupRepository, groupMemberRepository);
        locks.verify(groupRepository).findByIdForUpdate(9L);
        locks.verify(groupMemberRepository).findLeaderByGroupIdForUpdate(9L);
        locks.verify(groupMemberRepository).findByGroupIdAndMemberIdForUpdate(9L, 8L);
    }

    @Test
    void rejectsSameOrNonmemberLeaderTransfer() {
        Group group = group("그룹", 5);
        Member admin = member(99L, "관리자", Role.ADMIN);
        Member oldMember = member(7L, "기존", Role.USER);
        GroupMember oldLeader = GroupMember.leader(group, oldMember);
        when(groupRepository.findByIdForUpdate(9L)).thenReturn(Optional.of(group));
        when(memberService.getEntityOrThrow(99L)).thenReturn(admin);
        when(groupMemberRepository.findLeaderByGroupIdForUpdate(9L))
                .thenReturn(Optional.of(oldLeader));

        assertGroupError(
                () -> service.changeLeader(9L, 99L, 7L),
                ErrorCode.GROUP_LEADER_NOT_CHANGED
        );
        when(groupMemberRepository.findByGroupIdAndMemberIdForUpdate(9L, 8L))
                .thenReturn(Optional.empty());
        assertGroupError(
                () -> service.changeLeader(9L, 99L, 8L),
                ErrorCode.GROUP_NEW_LEADER_NOT_MEMBER
        );
    }

    @Test
    void deletesGroupAndExposesLeaderMembershipBoundary() {
        Group group = group("그룹", 5);
        Member admin = member(99L, "관리자", Role.ADMIN);
        when(groupRepository.findByIdForUpdate(9L)).thenReturn(Optional.of(group));
        when(memberService.getEntityOrThrow(99L)).thenReturn(admin);
        when(groupMemberRepository.existsByMemberIdAndRole(7L, GroupMemberRole.LEADER))
                .thenReturn(true);

        service.delete(9L, 99L);

        verify(groupRepository).deleteByIdWithCascade(9L);
        assertThat(service.hasLeaderMembership(7L)).isTrue();
    }

    private CreateGroupRequest alwaysRequest(String name) {
        return new CreateGroupRequest(
                GroupType.STUDY,
                name,
                "소개",
                10,
                RecruitmentMode.ALWAYS,
                null,
                null
        );
    }

    private Group group(String name, int capacity) {
        Group group = Group.create(
                name, "소개", GroupType.STUDY, capacity,
                RecruitmentMode.ALWAYS, null, null
        );
        ReflectionTestUtils.setField(group, "id", 9L);
        return group;
    }

    private Member member(Long id, String name, Role role) {
        Member member = mock(Member.class);
        when(member.getId()).thenReturn(id);
        when(member.getName()).thenReturn(name);
        when(member.getRole()).thenReturn(role);
        return member;
    }

    private void arrangeLeader(Group group, Member leader) {
        when(groupRepository.findByIdForUpdate(9L)).thenReturn(Optional.of(group));
        when(memberService.getEntityOrThrow(7L)).thenReturn(leader);
        when(groupMemberRepository.findByGroupIdAndMemberId(9L, 7L))
                .thenReturn(Optional.of(GroupMember.leader(group, leader)));
    }

    private void assertGroupError(Runnable action, ErrorCode errorCode) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(GroupClientException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(errorCode));
    }
}
