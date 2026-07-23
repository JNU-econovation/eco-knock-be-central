package jnu.econovation.ecoknockbecentral.group.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jnu.econovation.ecoknockbecentral.group.model.vo.GroupType;

public record UpdateGroupDetailRequest(
        @NotNull
        @Schema(description = "그룹 유형", example = "STUDY")
        GroupType type,

        @NotBlank
        @Size(max = 100)
        @Schema(description = "그룹 소개. 앞뒤 공백 제거 후 1~100자", example = "함께 백엔드를 공부합니다.")
        String introduction,

        @NotNull
        @Min(1)
        @Max(50)
        @Schema(description = "그룹장을 포함한 정원", example = "10", minimum = "1", maximum = "50")
        Integer capacity
) {
    public UpdateGroupDetailRequest {
        introduction = introduction == null ? null : introduction.trim();
    }
}
