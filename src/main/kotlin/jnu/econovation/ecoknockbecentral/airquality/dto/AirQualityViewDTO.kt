package jnu.econovation.ecoknockbecentral.airquality.dto

import jnu.econovation.ecoknockbecentral.airquality.readmodel.entity.*
import jnu.econovation.ecoknockbecentral.common.exception.server.InternalServerException
import jnu.econovation.ecoknockbecentral.common.toZonedDateTime
import java.time.ZonedDateTime

data class AirQualityViewDTO(
    val timeUnit: String,
    val content: AirQualityViewContent
) {
    companion object {
        fun from(entity: AirQualityView): AirQualityViewDTO {
            val timeUnit = when (entity) {
                is AirQuality1dView -> "1d"
                is AirQuality1hView -> "1h"
                is AirQuality1mView -> "1m"
                is AirQuality4hView -> "4h"
                is AirQuality5mView -> "5m"
                is AirQuality15mView -> "15m"

                else -> throw InternalServerException(
                    IllegalStateException("알려지지 않은 타입의 AirQualityView -> ${entity.javaClass.name}")
                )
            }

            val content = AirQualityViewContent(
                start = entity.bucketStart.toZonedDateTime(),
                end = entity.bucketEnd.toZonedDateTime(),
                avgPM25 = entity.avgPm25,
                maxPM25 = entity.maxPm25,
                minPM25 = entity.minPm25,
                avgHumidity = entity.avgHumidity,
                avgTemperature = entity.avgTemperature,
                avgEco2PPM = entity.avgEco2,
                avgBvocPPM = entity.avgBvoc,
                sampleCount = entity.sampleCount
            )

            return AirQualityViewDTO(timeUnit = timeUnit, content = content)
        }
    }
}

data class AirQualityViewContent(
    val start: ZonedDateTime,
    val end: ZonedDateTime,
    val avgPM25: Double,
    val maxPM25: Int,
    val minPM25: Int,
    val avgHumidity: Double,
    val avgTemperature: Double,
    val avgEco2PPM: Double,
    val avgBvocPPM: Double,
    val sampleCount: Long
)
