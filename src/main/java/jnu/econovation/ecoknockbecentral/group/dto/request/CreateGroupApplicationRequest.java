package jnu.econovation.ecoknockbecentral.group.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jnu.econovation.ecoknockbecentral.group.model.entity.GroupApplication;

public record CreateGroupApplicationRequest(
        @NotBlank
        @Size(
                min = GroupApplication.MIN_CONTENT_LENGTH,
                max = GroupApplication.MAX_CONTENT_LENGTH
        )
        @Schema(description = "지원 내용", example = "함께 활동하고 싶습니다.", minLength = 1, maxLength = 20)
        String content
) {
    public CreateGroupApplicationRequest {
        content = content == null ? null : content.trim();
    }
}
