package jnu.econovation.ecoknockbecentral.group.service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import jnu.econovation.ecoknockbecentral.common.exception.constants.ErrorCode;
import jnu.econovation.ecoknockbecentral.group.dto.request.BrowseGroupsRequest;
import jnu.econovation.ecoknockbecentral.group.dto.request.CreateGroupRequest;
import jnu.econovation.ecoknockbecentral.group.dto.request.UpdateGroupDetailRequest;
import jnu.econovation.ecoknockbecentral.group.dto.request.UpdateGroupNameRequest;
import jnu.econovation.ecoknockbecentral.group.dto.request.UpdateGroupRecruitmentRequest;
import jnu.econovation.ecoknockbecentral.group.dto.response.BrowseGroupResponse;
import jnu.econovation.ecoknockbecentral.group.dto.response.CreateGroupResponse;
import jnu.econovation.ecoknockbecentral.group.dto.response.GroupDetailResponse;
import jnu.econovation.ecoknockbecentral.group.dto.response.GroupMemberResponse;
import jnu.econovation.ecoknockbecentral.group.dto.response.ManageGroupMemberResponse;
import jnu.econovation.ecoknockbecentral.group.dto.response.MyGroupResponse;
import jnu.econovation.ecoknockbecentral.group.exception.GroupClientException;
import jnu.econovation.ecoknockbecentral.group.model.entity.Group;
import jnu.econovation.ecoknockbecentral.group.model.entity.GroupMember;
import jnu.econovation.ecoknockbecentral.group.model.vo.GroupMemberRole;
import jnu.econovation.ecoknockbecentral.group.model.vo.RecruitmentMode;
import jnu.econovation.ecoknockbecentral.group.repository.GroupBrowseRow;
import jnu.econovation.ecoknockbecentral.group.repository.GroupMemberRepository;
import jnu.econovation.ecoknockbecentral.group.repository.GroupRepository;
import jnu.econovation.ecoknockbecentral.member.model.entity.Member;
import jnu.econovation.ecoknockbecentral.member.model.vo.Role;
import jnu.econovation.ecoknockbecentral.member.service.MemberService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final MemberService memberService;

    public GroupService(
            GroupRepository groupRepository,
            GroupMemberRepository groupMemberRepository,
            MemberService memberService
    ) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.memberService = memberService;
    }

    @Transactional
    public CreateGroupResponse create(Long creatorId, CreateGroupRequest request) {
        String name = request.name().trim();
        if (groupRepository.existsByName(name)) {
            throw new GroupClientException(ErrorCode.GROUP_NAME_DUPLICATED);
        }
        validateCapacity(request.capacity());
        validateRecruitmentPeriod(request);

        Member creator = memberService.getEntityOrThrow(creatorId);
        Group group = Group.create(
                name,
                request.introduction(),
                request.type(),
                request.capacity(),
                request.recruitmentMode(),
                toInstant(request.recruitmentStartAt()),
                toInstant(request.recruitmentEndAt())
        );

        try {
            groupRepository.saveAndFlush(group);
        } catch (DataIntegrityViolationException exception) {
            throw new GroupClientException(ErrorCode.GROUP_NAME_DUPLICATED);
        }
        groupMemberRepository.save(GroupMember.leader(group, creator));
        return new CreateGroupResponse(group.getId());
    }

    @Transactional(readOnly = true)
    public List<MyGroupResponse> getMyGroups(Long memberId) {
        return groupMemberRepository.findAllByMemberIdWithGroup(memberId).stream()
                .map(groupMember -> new MyGroupResponse(
                        groupMember.getGroup().getId(),
                        groupMember.getGroup().getType(),
                        groupMember.getGroup().getName(),
                        groupMember.getRole() == GroupMemberRole.LEADER
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BrowseGroupResponse> browse(BrowseGroupsRequest request) {
        Instant now = Instant.now();
        return groupRepository.findAllForBrowse(
                        request.excludeClosed(),
                        request.sort(),
                        now
                ).stream()
                .map(row -> toBrowseResponse(row, now))
                .toList();
    }

    @Transactional(readOnly = true)
    public GroupDetailResponse getDetail(Long groupId, Long requesterId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupClientException(ErrorCode.GROUP_NOT_FOUND));
        List<GroupMember> groupMembers = groupMemberRepository.findAllByGroupIdWithMember(groupId);
        GroupMember leader = groupMembers.stream()
                .filter(groupMember -> groupMember.getRole() == GroupMemberRole.LEADER)
                .findFirst()
                .orElseThrow(() -> new GroupClientException(ErrorCode.GROUP_MEMBER_NOT_FOUND));

        boolean isMember = groupMembers.stream()
                .anyMatch(groupMember -> groupMember.getMember().getId().equals(requesterId));
        boolean isLeader = leader.getMember().getId().equals(requesterId);
        int memberCount = groupMembers.size();

        return new GroupDetailResponse(
                group.getId(),
                group.getType(),
                group.getName(),
                group.getDescription(),
                memberCount,
                group.getCapacity(),
                group.getRecruitmentMode(),
                group.getRecruitmentStatus(memberCount, Instant.now()),
                group.getRecruitmentStartAt(),
                group.getRecruitmentEndAt(),
                leader.getMember().getName(),
                groupMembers.stream()
                        .map(groupMember -> new GroupMemberResponse(groupMember.getMember().getName()))
                        .toList(),
                isMember,
                isLeader
        );
    }

    @Transactional
    public void updateName(Long groupId, Long requesterId, UpdateGroupNameRequest request) {
        Group group = getGroupForUpdate(groupId);
        Member requester = memberService.getEntityOrThrow(requesterId);
        requireLeaderOrAdmin(groupId, requester);

        String name = request.name().trim();
        if (group.getName().equals(name)) {
            return;
        }
        if (groupRepository.existsByNameAndIdNot(name, groupId)) {
            throw new GroupClientException(ErrorCode.GROUP_NAME_DUPLICATED);
        }
        group.updateName(name);
        try {
            groupRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new GroupClientException(ErrorCode.GROUP_NAME_DUPLICATED);
        }
    }

    @Transactional
    public void updateDetails(
            Long groupId,
            Long requesterId,
            UpdateGroupDetailRequest request
    ) {
        Group group = getGroupForUpdate(groupId);
        Member requester = memberService.getEntityOrThrow(requesterId);
        requireLeaderOrAdmin(groupId, requester);

        int memberCount = Math.toIntExact(groupMemberRepository.countByGroupId(groupId));
        if (request.capacity() < memberCount) {
            throw new GroupClientException(ErrorCode.GROUP_CAPACITY_INVALID);
        }
        group.updateDetails(request.introduction(), request.type(), request.capacity());
    }

    @Transactional
    public void updateRecruitment(
            Long groupId,
            Long requesterId,
            UpdateGroupRecruitmentRequest request
    ) {
        Group group = getGroupForUpdate(groupId);
        Member requester = memberService.getEntityOrThrow(requesterId);
        requireLeaderOrAdmin(groupId, requester);
        try {
            group.updateRecruitment(
                    request.recruitmentMode(),
                    toInstant(request.recruitmentStartAt()),
                    toInstant(request.recruitmentEndAt())
            );
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new GroupClientException(ErrorCode.GROUP_RECRUITMENT_PERIOD_INVALID);
        }
    }

    @Transactional(readOnly = true)
    public List<ManageGroupMemberResponse> getMembersForManagement(
            Long groupId,
            Long requesterId
    ) {
        groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupClientException(ErrorCode.GROUP_NOT_FOUND));
        Member requester = memberService.getEntityOrThrow(requesterId);
        requireLeaderOrAdmin(groupId, requester);
        return groupMemberRepository.findAllForManagement(groupId).stream()
                .sorted(Comparator
                        .comparing((GroupMember membership) ->
                                membership.getRole() == GroupMemberRole.LEADER ? 0 : 1)
                        .thenComparing(membership -> membership.getMember().getName())
                        .thenComparing(membership -> membership.getMember().getId()))
                .map(ManageGroupMemberResponse::from)
                .toList();
    }

    @Transactional
    public void removeMember(Long groupId, Long requesterId, Long memberId) {
        getGroupForUpdate(groupId);
        Member requester = memberService.getEntityOrThrow(requesterId);
        requireLeaderOrAdmin(groupId, requester);
        GroupMember target = groupMemberRepository.findByGroupIdAndMemberIdForUpdate(
                        groupId,
                        memberId
                )
                .orElseThrow(() -> new GroupClientException(ErrorCode.GROUP_MEMBER_NOT_FOUND));
        if (target.getRole() == GroupMemberRole.LEADER) {
            throw new GroupClientException(ErrorCode.GROUP_LEADER_CANNOT_BE_REMOVED);
        }
        groupMemberRepository.delete(target);
    }

    @Transactional
    public void changeLeader(Long groupId, Long requesterId, Long memberId) {
        getGroupForUpdate(groupId);
        Member requester = memberService.getEntityOrThrow(requesterId);
        requireLeaderOrAdmin(groupId, requester);
        GroupMember currentLeader = groupMemberRepository.findLeaderByGroupIdForUpdate(groupId)
                .orElseThrow(() -> new GroupClientException(ErrorCode.GROUP_MEMBER_NOT_FOUND));
        if (currentLeader.getMember().getId().equals(memberId)) {
            throw new GroupClientException(ErrorCode.GROUP_LEADER_NOT_CHANGED);
        }
        GroupMember newLeader = groupMemberRepository.findByGroupIdAndMemberIdForUpdate(
                        groupId,
                        memberId
                )
                .orElseThrow(() -> new GroupClientException(
                        ErrorCode.GROUP_NEW_LEADER_NOT_MEMBER
                ));

        currentLeader.demoteToMember();
        groupMemberRepository.flush();
        newLeader.promoteToLeader();
        groupMemberRepository.flush();
    }

    @Transactional
    public void delete(Long groupId, Long requesterId) {
        getGroupForUpdate(groupId);
        Member requester = memberService.getEntityOrThrow(requesterId);
        requireLeaderOrAdmin(groupId, requester);
        groupRepository.deleteByIdWithCascade(groupId);
    }

    @Transactional(readOnly = true)
    public boolean hasLeaderMembership(long memberId) {
        return groupMemberRepository.existsByMemberIdAndRole(memberId, GroupMemberRole.LEADER);
    }

    private BrowseGroupResponse toBrowseResponse(GroupBrowseRow row, Instant now) {
        int memberCount = Math.toIntExact(row.currentMemberCount());
        return new BrowseGroupResponse(
                row.group().getId(),
                row.group().getName(),
                memberCount,
                row.group().getCapacity(),
                row.leaderName(),
                row.group().getRecruitmentStatus(memberCount, now)
        );
    }

    private Group getGroupForUpdate(Long groupId) {
        return groupRepository.findByIdForUpdate(groupId)
                .orElseThrow(() -> new GroupClientException(ErrorCode.GROUP_NOT_FOUND));
    }

    private void requireLeaderOrAdmin(Long groupId, Member requester) {
        if (requester.getRole() == Role.ADMIN) {
            return;
        }
        GroupMember membership = groupMemberRepository.findByGroupIdAndMemberId(
                        groupId,
                        requester.getId()
                )
                .orElseThrow(() -> new GroupClientException(
                        ErrorCode.GROUP_LEADER_PERMISSION_REQUIRED
                ));
        if (membership.getRole() != GroupMemberRole.LEADER) {
            throw new GroupClientException(ErrorCode.GROUP_LEADER_PERMISSION_REQUIRED);
        }
    }

    private void validateCapacity(int capacity) {
        if (capacity < Group.MIN_CAPACITY || capacity > Group.MAX_CAPACITY) {
            throw new GroupClientException(ErrorCode.GROUP_CAPACITY_INVALID);
        }
    }

    private void validateRecruitmentPeriod(CreateGroupRequest request) {
        if (request.recruitmentMode() == RecruitmentMode.ALWAYS) {
            if (request.recruitmentStartAt() != null || request.recruitmentEndAt() != null) {
                throw new GroupClientException(ErrorCode.GROUP_RECRUITMENT_PERIOD_INVALID);
            }
            return;
        }
        if (request.recruitmentStartAt() == null
                || request.recruitmentEndAt() == null
                || request.recruitmentStartAt().isAfter(request.recruitmentEndAt())) {
            throw new GroupClientException(ErrorCode.GROUP_RECRUITMENT_PERIOD_INVALID);
        }
    }

    private Instant toInstant(java.time.ZonedDateTime value) {
        return value == null ? null : value.toInstant();
    }
}
