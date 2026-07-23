package jnu.econovation.ecoknockbecentral.group.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jnu.econovation.ecoknockbecentral.group.model.entity.GroupMember;
import jnu.econovation.ecoknockbecentral.group.model.vo.GroupMemberRole;

public record ManageGroupMemberResponse(
        @Schema(description = "회원 ID", example = "42")
        Long memberId,
        @Schema(description = "회원 이름", example = "김이코")
        String name,
        @Schema(description = "그룹장 여부", example = "false")
        boolean leader
) {
    public static ManageGroupMemberResponse from(GroupMember groupMember) {
        return new ManageGroupMemberResponse(
                groupMember.getMember().getId(),
                groupMember.getMember().getName(),
                groupMember.getRole() == GroupMemberRole.LEADER
        );
    }
}
