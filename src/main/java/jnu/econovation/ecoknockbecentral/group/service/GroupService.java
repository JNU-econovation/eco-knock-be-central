package jnu.econovation.ecoknockbecentral.group.service;

import java.time.Instant;
import java.util.List;
import jnu.econovation.ecoknockbecentral.common.exception.constants.ErrorCode;
import jnu.econovation.ecoknockbecentral.group.dto.request.BrowseGroupsRequest;
import jnu.econovation.ecoknockbecentral.group.dto.request.CreateGroupRequest;
import jnu.econovation.ecoknockbecentral.group.dto.response.BrowseGroupResponse;
import jnu.econovation.ecoknockbecentral.group.dto.response.CreateGroupResponse;
import jnu.econovation.ecoknockbecentral.group.dto.response.GroupDetailResponse;
import jnu.econovation.ecoknockbecentral.group.dto.response.GroupMemberResponse;
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
