package jnu.econovation.ecoknockbecentral.group.dto.response;

import jnu.econovation.ecoknockbecentral.group.model.vo.GroupType;

public record MyGroupResponse(
        Long groupId,
        GroupType type,
        String name,
        boolean isLeader
) {
}
