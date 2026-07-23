package jnu.econovation.ecoknockbecentral.group.dto.response;

import java.time.Instant;
import jnu.econovation.ecoknockbecentral.group.model.entity.GroupApplication;

public record GroupApplicationResponse(
        Long applicationId,
        Long applicantMemberId,
        String applicantName,
        String content,
        Instant appliedAt
) {

    public static GroupApplicationResponse from(GroupApplication application) {
        return new GroupApplicationResponse(
                application.getId(),
                application.getApplicant().getId(),
                application.getApplicant().getName(),
                application.getContent(),
                application.getCreatedAt()
        );
    }
}
