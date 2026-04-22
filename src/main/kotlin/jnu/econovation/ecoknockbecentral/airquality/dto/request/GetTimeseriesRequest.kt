package jnu.econovation.ecoknockbecentral.airquality.dto.request

import java.time.OffsetDateTime

data class GetTimeseriesRequest(
    val resolution: AirQualityResolution,
    val from: OffsetDateTime,
    val to: OffsetDateTime
)