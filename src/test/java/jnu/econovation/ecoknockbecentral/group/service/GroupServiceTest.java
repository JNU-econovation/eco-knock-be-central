package jnu.econovation.ecoknockbecentral.group.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
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
import jnu.econovation.ecoknockbecentral.group.exception.GroupClientException;
import jnu.econovation.ecoknockbecentral.group.model.entity.Group;
import jnu.econovation.ecoknockbecentral.group.model.entity.GroupMember;
import jnu.econovation.ecoknockbecentral.group.model.vo.GroupMemberRole;
import jnu.econovation.ecoknockbecentral.group.model.vo.GroupSort;
import jnu.econovation.ecoknockbecentral.group.model.vo.GroupType;
import jnu.econovation.ecoknockbecentral.group.model.vo.RecruitmentMode;
import jnu.econovation.ecoknockbecentral.group.model.vo.RecruitmentStatus;
import jnu.econovation.ecoknockbecentral.group.repository.GroupBrowseRow;
import jnu.econovation.ecoknockbecentral.group.repository.GroupMemberRepository;
import jnu.econovation.ecoknockbecentral.group.repository.GroupRepository;
import jnu.econovation.ecoknockbecentral.member.model.entity.Member;
import jnu.econovation.ecoknockbecentral.member.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

class GroupServiceTest {

    private GroupRepository groupRepository;
    private GroupMemberRepository groupMemberRepository;
    private MemberService memberService;
    private GroupService service;

    @BeforeEach
    void setUp() {
        groupRepository = mock(GroupRepository.class);
        groupMemberRepository = mock(GroupMemberRepository.class);
        memberService = mock(MemberService.class);
        service = new GroupService(groupRepository, groupMemberRepository, memberService);
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
                any(Instant.class)
        ))
                .thenReturn(List.of(new GroupBrowseRow(group, 2, "리더")));

        var result = service.browse(new BrowseGroupsRequest(false, GroupSort.NAME_ASC));

        assertThat(result).singleElement().satisfies(response -> {
            assertThat(response.currentMemberCount()).isEqualTo(2);
            assertThat(response.leaderName()).isEqualTo("리더");
            assertThat(response.recruitmentStatus()).isEqualTo(RecruitmentStatus.CLOSED);
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

        var response = service.getDetail(9L, 7L);

        assertThat(response.isMember()).isTrue();
        assertThat(response.isLeader()).isTrue();
        assertThat(response.members()).containsExactly(new jnu.econovation.ecoknockbecentral.group.dto.response.GroupMemberResponse("리더"));
        assertThat(response.recruitmentStatus()).isEqualTo(RecruitmentStatus.RECRUITING);
    }

    @Test
    void rejectsMissingDetail() {
        when(groupRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getDetail(404L, 7L))
                .isInstanceOfSatisfying(GroupClientException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.GROUP_NOT_FOUND));
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
}
