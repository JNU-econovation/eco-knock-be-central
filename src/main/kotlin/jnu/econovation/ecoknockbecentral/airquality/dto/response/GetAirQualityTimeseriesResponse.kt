package jnu.econovation.ecoknockbecentral.airquality.dto.response

import jnu.econovation.ecoknockbecentral.airquality.readmodel.entity.AirQualityView
import jnu.econovation.ecoknockbecentral.common.toZonedDateTime
import java.time.ZonedDateTime

data class AirQualityTimeseriesPointResponse(
    val time: ZonedDateTime,
    val end: ZonedDateTime,
    val pm25: Double,
    val pm25Min: Int,
    val pm25Max: Int,
    val humidity: Double,
    val temperature: Double,
    val eco2: Double,
    val bvoc: Double,
    val sampleCount: Long,
) {
    companion object {
        fun from(entity: AirQualityView): AirQualityTimeseriesPointResponse {
            return AirQualityTimeseriesPointResponse(
                time = entity.bucketStart.toZonedDateTime(),
                end = entity.bucketEnd.toZonedDateTime(),
                pm25 = entity.avgPm25,
                pm25Min = entity.minPm25,
                pm25Max = entity.maxPm25,
                humidity = entity.avgHumidity,
                temperature = entity.avgTemperature,
                eco2 = entity.avgEco2,
                bvoc = entity.avgBvoc,
                sampleCount = entity.sampleCount,
            )
        }
    }
}
