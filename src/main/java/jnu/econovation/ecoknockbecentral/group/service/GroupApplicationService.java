package jnu.econovation.ecoknockbecentral.group.service;

import java.time.Instant;
import java.util.List;
import jnu.econovation.ecoknockbecentral.common.exception.constants.ErrorCode;
import jnu.econovation.ecoknockbecentral.group.dto.response.GroupApplicationResponse;
import jnu.econovation.ecoknockbecentral.group.exception.GroupClientException;
import jnu.econovation.ecoknockbecentral.group.model.entity.Group;
import jnu.econovation.ecoknockbecentral.group.model.entity.GroupApplication;
import jnu.econovation.ecoknockbecentral.group.model.entity.GroupMember;
import jnu.econovation.ecoknockbecentral.group.model.vo.GroupApplicationStatus;
import jnu.econovation.ecoknockbecentral.group.model.vo.GroupMemberRole;
import jnu.econovation.ecoknockbecentral.group.model.vo.RecruitmentStatus;
import jnu.econovation.ecoknockbecentral.group.repository.GroupApplicationRepository;
import jnu.econovation.ecoknockbecentral.group.repository.GroupMemberRepository;
import jnu.econovation.ecoknockbecentral.group.repository.GroupRepository;
import jnu.econovation.ecoknockbecentral.member.model.entity.Member;
import jnu.econovation.ecoknockbecentral.member.model.vo.Role;
import jnu.econovation.ecoknockbecentral.member.service.MemberService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GroupApplicationService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupApplicationRepository groupApplicationRepository;
    private final MemberService memberService;

    public GroupApplicationService(
            GroupRepository groupRepository,
            GroupMemberRepository groupMemberRepository,
            GroupApplicationRepository groupApplicationRepository,
            MemberService memberService
    ) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.groupApplicationRepository = groupApplicationRepository;
        this.memberService = memberService;
    }

    @Transactional
    public void create(Long groupId, Long applicantId, String content) {
        Group group = groupRepository.findByIdForUpdate(groupId)
                .orElseThrow(() -> new GroupClientException(ErrorCode.GROUP_NOT_FOUND));
        Member applicant = memberService.getEntityOrThrow(applicantId);
        if (applicant.getRole() == Role.GUEST) {
            throw new GroupClientException(ErrorCode.GROUP_ACCESS_DENIED);
        }
        if (groupMemberRepository.existsByGroupIdAndMemberId(groupId, applicantId)) {
            throw new GroupClientException(ErrorCode.GROUP_APPLICANT_ALREADY_MEMBER);
        }
        if (groupApplicationRepository.existsByGroupIdAndApplicantIdAndStatus(
                groupId,
                applicantId,
                GroupApplicationStatus.PENDING
        )) {
            throw new GroupClientException(ErrorCode.GROUP_APPLICATION_ALREADY_PENDING);
        }

        int memberCount = Math.toIntExact(groupMemberRepository.countByGroupId(groupId));
        RecruitmentStatus status = group.getRecruitmentStatus(memberCount, Instant.now());
        if (status != RecruitmentStatus.RECRUITING
                && status != RecruitmentStatus.ALWAYS_RECRUITING) {
            throw new GroupClientException(ErrorCode.GROUP_RECRUITMENT_CLOSED);
        }

        GroupApplication application;
        try {
            application = GroupApplication.pending(group, applicant, content);
        } catch (IllegalArgumentException exception) {
            throw new GroupClientException(ErrorCode.GROUP_APPLICATION_CONTENT_INVALID);
        }
        try {
            groupApplicationRepository.saveAndFlush(application);
        } catch (DataIntegrityViolationException exception) {
            throw new GroupClientException(ErrorCode.GROUP_APPLICATION_ALREADY_PENDING);
        }
    }

    @Transactional(readOnly = true)
    public List<GroupApplicationResponse> getPendingApplications(
            Long groupId,
            Long requesterId
    ) {
        groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupClientException(ErrorCode.GROUP_NOT_FOUND));
        Member requester = memberService.getEntityOrThrow(requesterId);
        requireMemberOrAdmin(groupId, requester);

        return groupApplicationRepository.findAllByGroupIdAndStatusWithApplicant(
                        groupId,
                        GroupApplicationStatus.PENDING
                ).stream()
                .map(GroupApplicationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public GroupApplicationResponse getPendingApplication(
            Long groupId,
            Long applicationId,
            Long requesterId
    ) {
        groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupClientException(ErrorCode.GROUP_NOT_FOUND));
        Member requester = memberService.getEntityOrThrow(requesterId);
        requireMemberOrAdmin(groupId, requester);

        GroupApplication application =
                groupApplicationRepository.findByIdAndGroupIdAndStatusWithApplicant(
                                applicationId,
                                groupId,
                                GroupApplicationStatus.PENDING
                        )
                        .orElseThrow(() -> new GroupClientException(
                                ErrorCode.GROUP_APPLICATION_NOT_FOUND
                        ));
        return GroupApplicationResponse.from(application);
    }

    @Transactional
    public void accept(Long groupId, Long applicationId, Long requesterId) {
        Group group = groupRepository.findByIdForUpdate(groupId)
                .orElseThrow(() -> new GroupClientException(ErrorCode.GROUP_NOT_FOUND));
        Member requester = memberService.getEntityOrThrow(requesterId);
        requireLeaderOrAdmin(groupId, requester);
        GroupApplication application =
                groupApplicationRepository.findByIdAndGroupIdForUpdate(applicationId, groupId)
                        .orElseThrow(() -> new GroupClientException(
                                ErrorCode.GROUP_APPLICATION_NOT_FOUND
                        ));
        requirePending(application);

        if (groupMemberRepository.countByGroupId(groupId) >= group.getCapacity()) {
            throw new GroupClientException(ErrorCode.GROUP_CAPACITY_REACHED);
        }
        Long applicantId = application.getApplicant().getId();
        if (groupMemberRepository.existsByGroupIdAndMemberId(groupId, applicantId)) {
            throw new GroupClientException(ErrorCode.GROUP_MEMBER_ALREADY_EXISTS);
        }

        try {
            groupMemberRepository.saveAndFlush(
                    GroupMember.member(group, application.getApplicant())
            );
        } catch (DataIntegrityViolationException exception) {
            throw new GroupClientException(ErrorCode.GROUP_MEMBER_ALREADY_EXISTS);
        }
        application.accept();
    }

    @Transactional
    public void reject(Long groupId, Long applicationId, Long requesterId) {
        groupRepository.findByIdForUpdate(groupId)
                .orElseThrow(() -> new GroupClientException(ErrorCode.GROUP_NOT_FOUND));
        Member requester = memberService.getEntityOrThrow(requesterId);
        requireLeaderOrAdmin(groupId, requester);
        GroupApplication application =
                groupApplicationRepository.findByIdAndGroupIdForUpdate(applicationId, groupId)
                        .orElseThrow(() -> new GroupClientException(
                                ErrorCode.GROUP_APPLICATION_NOT_FOUND
                        ));
        requirePending(application);
        application.reject();
    }

    private void requireMemberOrAdmin(Long groupId, Member requester) {
        if (requester.getRole() != Role.ADMIN
                && !groupMemberRepository.existsByGroupIdAndMemberId(
                        groupId,
                        requester.getId()
                )) {
            throw new GroupClientException(ErrorCode.GROUP_ACCESS_DENIED);
        }
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

    private void requirePending(GroupApplication application) {
        if (application.getStatus() != GroupApplicationStatus.PENDING) {
            throw new GroupClientException(ErrorCode.GROUP_APPLICATION_ALREADY_PROCESSED);
        }
    }
}
