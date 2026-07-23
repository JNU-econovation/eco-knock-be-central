package jnu.econovation.ecoknockbecentral.group.dto.response;

import jnu.econovation.ecoknockbecentral.group.model.entity.GroupMember;
import jnu.econovation.ecoknockbecentral.group.model.vo.GroupMemberRole;

public record GroupMemberIdentityResponse(
        Long memberId,
        String name,
        boolean leader
) {

    public static GroupMemberIdentityResponse from(GroupMember groupMember) {
        return new GroupMemberIdentityResponse(
                groupMember.getMember().getId(),
                groupMember.getMember().getName(),
                groupMember.getRole() == GroupMemberRole.LEADER
        );
    }
}
