package jnu.econovation.ecoknockbecentral.group.repository;

import java.time.Instant;
import java.util.List;
import jnu.econovation.ecoknockbecentral.group.model.vo.GroupSort;

public interface GroupRepositoryCustom {

    List<GroupBrowseRow> findAllForBrowse(
            boolean excludeClosed,
            GroupSort sort,
            Instant now,
            Long requesterId
    );

    List<GroupBrowseRow> findAllForMember(Long memberId, Instant now);
}
