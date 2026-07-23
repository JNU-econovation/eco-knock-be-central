package jnu.econovation.ecoknockbecentral.airquality.dto.rest.request

import io.swagger.v3.oas.annotations.media.Schema
import jnu.econovation.ecoknockbecentral.airquality.model.vo.AirQualityResolution
import java.time.OffsetDateTime

data class GetTimeseriesRequest(
    @field:Schema(description = "집계 해상도", allowableValues = ["1m", "5m", "15m", "1h", "4h", "1d"], example = "5m")
    val resolution: AirQualityResolution,
    @field:Schema(description = "조회 시작 시각", example = "2026-06-30T00:00:00Z")
    val from: OffsetDateTime,
    @field:Schema(description = "조회 종료 시각", example = "2026-06-30T01:00:00Z")
    val to: OffsetDateTime
)
