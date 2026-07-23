package jnu.econovation.ecoknockbecentral.group.dto.response;

import jnu.econovation.ecoknockbecentral.group.model.vo.GroupType;
import jnu.econovation.ecoknockbecentral.group.model.vo.RecruitmentStatus;

public record GroupSummaryResponse(
        Long groupId,
        GroupType type,
        String name,
        int currentMemberCount,
        int capacity,
        String leaderName,
        RecruitmentStatus recruitmentStatus,
        boolean isMember,
        boolean isLeader
) {
}
