package jnu.econovation.ecoknockbecentral.airquality.dto.rest.request

import io.swagger.v3.oas.annotations.media.Schema
import jnu.econovation.ecoknockbecentral.airquality.exception.BadAirQualityHistoryLimitException
import jnu.econovation.ecoknockbecentral.airquality.model.vo.AirQualityResolution
import java.time.OffsetDateTime

data class GetTimeseriesHistoryRequest(
    @field:Schema(description = "집계 해상도", allowableValues = ["1m", "5m", "15m", "1h", "4h", "1d"], example = "5m")
    val resolution: AirQualityResolution,
    @field:Schema(description = "이 시각 이전 데이터 조회", example = "2026-06-30T01:00:00Z")
    val before: OffsetDateTime,
    @field:Schema(description = "조회 개수. 1 이상 500 이하", minimum = "1", maximum = "500", example = "50")
    val limit: Int
) {
    companion object {
        const val MIN_LIMIT = 1
        const val MAX_LIMIT = 500
    }

    init {
        if (limit !in MIN_LIMIT..MAX_LIMIT) {
            throw BadAirQualityHistoryLimitException()
        }
    }
}
