package jnu.econovation.ecoknockbecentral.airquality.dto.internal

import jnu.econovation.ecoknockbecentral.airquality.model.vo.AirQualityResolution
import jnu.econovation.ecoknockbecentral.airquality.dto.rest.request.GetTimeseriesHistoryRequest
import jnu.econovation.ecoknockbecentral.airquality.dto.rest.request.GetTimeseriesRequest
import java.time.Instant

data class GetTimeseriesDTO(
    val resolution: AirQualityResolution,
    val from: Instant,
    val to: Instant
) {
    companion object {
        fun from(request: GetTimeseriesRequest) : GetTimeseriesDTO {
            return GetTimeseriesDTO(
                resolution = request.resolution,
                from = request.from.toInstant(),
                to = request.to.toInstant()
            )
        }
    }
}

data class GetTimeseriesHistoryDTO(
    val resolution: AirQualityResolution,
    val before: Instant,
    val limit: Int
) {
    companion object {
        fun from(request: GetTimeseriesHistoryRequest) : GetTimeseriesHistoryDTO {
            return GetTimeseriesHistoryDTO(
                resolution = request.resolution,
                before = request.before.toInstant(),
                limit = request.limit
            )
        }
    }
}
