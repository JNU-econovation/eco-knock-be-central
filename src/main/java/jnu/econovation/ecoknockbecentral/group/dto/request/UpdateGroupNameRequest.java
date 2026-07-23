package jnu.econovation.ecoknockbecentral.group.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateGroupNameRequest(
        @NotBlank
        @Size(max = 15)
        @Schema(description = "변경할 그룹명. 앞뒤 공백 제거 후 1~15자", example = "백엔드 스터디")
        String name
) {
    public UpdateGroupNameRequest {
        name = name == null ? null : name.trim();
    }
}
