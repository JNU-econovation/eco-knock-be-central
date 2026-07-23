package jnu.econovation.ecoknockbecentral.group.dto.response;

import java.time.Instant;
import java.util.List;
import jnu.econovation.ecoknockbecentral.group.model.vo.GroupType;
import jnu.econovation.ecoknockbecentral.group.model.vo.RecruitmentMode;
import jnu.econovation.ecoknockbecentral.group.model.vo.RecruitmentStatus;

public record GroupDetailResponse(
        Long groupId,
        GroupType type,
        String name,
        String introduction,
        int currentMemberCount,
        int capacity,
        RecruitmentMode recruitmentMode,
        RecruitmentStatus recruitmentStatus,
        Instant recruitmentStartAt,
        Instant recruitmentEndAt,
        String leaderName,
        List<GroupMemberResponse> members,
        boolean isMember,
        boolean isLeader
) {
}
