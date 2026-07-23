package jnu.econovation.ecoknockbecentral.group.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import jnu.econovation.ecoknockbecentral.group.model.vo.RecruitmentMode;

public record UpdateGroupRecruitmentRequest(
        @NotNull
        @Schema(description = "모집 방식", example = "PERIOD")
        RecruitmentMode recruitmentMode,

        @Schema(description = "기간 모집 시작 시각", example = "2026-08-01T09:00:00+09:00")
        ZonedDateTime recruitmentStartAt,

        @Schema(description = "기간 모집 종료 시각", example = "2026-08-31T18:00:00+09:00")
        ZonedDateTime recruitmentEndAt
) {
}
