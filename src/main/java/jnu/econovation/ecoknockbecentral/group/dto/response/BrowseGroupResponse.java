package jnu.econovation.ecoknockbecentral.group.dto.response;

import jnu.econovation.ecoknockbecentral.group.model.vo.RecruitmentStatus;

public record BrowseGroupResponse(
        Long groupId,
        String name,
        int currentMemberCount,
        int capacity,
        String leaderName,
        RecruitmentStatus recruitmentStatus
) {
}
