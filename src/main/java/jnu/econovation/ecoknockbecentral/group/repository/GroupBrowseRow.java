package jnu.econovation.ecoknockbecentral.group.repository;

import jnu.econovation.ecoknockbecentral.group.model.entity.Group;

public record GroupBrowseRow(
        Group group,
        long currentMemberCount,
        String leaderName
) {
}
