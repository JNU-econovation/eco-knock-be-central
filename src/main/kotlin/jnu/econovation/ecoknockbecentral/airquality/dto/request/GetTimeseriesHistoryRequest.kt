package jnu.econovation.ecoknockbecentral.airquality.dto.request

import jnu.econovation.ecoknockbecentral.airquality.exception.BadAirQualityHistoryLimitException
import java.time.OffsetDateTime

data class GetTimeseriesHistoryRequest(
    val resolution: AirQualityResolution,
    val before: OffsetDateTime,
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
