package jnu.econovation.ecoknockbecentral.group.dto.request;

import jnu.econovation.ecoknockbecentral.group.model.vo.GroupSort;

public record BrowseGroupsRequest(
        boolean excludeClosed,
        GroupSort sort
) {
}
