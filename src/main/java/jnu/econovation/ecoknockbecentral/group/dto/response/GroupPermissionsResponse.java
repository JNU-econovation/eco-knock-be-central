package jnu.econovation.ecoknockbecentral.group.dto.response;

public record GroupPermissionsResponse(
        boolean canViewSettings,
        boolean canEditGroup,
        boolean canManageMembers,
        boolean canViewApplications,
        boolean canReviewApplications,
        boolean canDeleteGroup,
        boolean canApply
) {
}
