package jnu.econovation.ecoknockbecentral.group.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ChangeGroupLeaderRequest(
        @NotNull
        @Positive
        @Schema(description = "새 그룹장의 회원 ID", example = "42")
        Long memberId
) {
}
