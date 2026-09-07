package jnu.econovation.ecoknockbecentral.airquality.dto.internal

import jnu.econovation.ecoknockbecentral.airquality.readmodel.entity.*
import jnu.econovation.ecoknockbecentral.common.exception.server.InternalServerException
import jnu.econovation.ecoknockbecentral.common.extension.toZonedDateTime
import java.time.ZonedDateTime

data class AirQualityViewDTO(
    val timeUnit: String,
    val content: AirQualityViewContent
) {
    companion object {
        fun from(entity: AirQualityAggregate): AirQualityViewDTO {
            val timeUnit = when (entity) {
                is AirQuality1mAggregate -> "1m"
                is AirQuality5mAggregate -> "5m"
                is AirQuality15mAggregate -> "15m"
                is AirQuality1hAggregate -> "1h"
                is AirQuality4hAggregate -> "4h"
                is AirQuality1dAggregate -> "1d"

                else -> throw InternalServerException(
                    IllegalStateException(
                        "알려지지 않은 타입의 AirQualityAggregate -> ${entity.javaClass.name}"
                    )
                )
            }

            val sampleCount = entity.sampleCount

            if (sampleCount <= 0) {
                throw InternalServerException(
                    IllegalStateException(
                        "AirQualityAggregate sampleCount가 0 이하입니다. bucketStart=${entity.bucketStart}"
                    )
                )
            }

            val count = sampleCount.toDouble()

            val content = AirQualityViewContent(
                start = entity.bucketStart.toZonedDateTime(),
                end = entity.bucketEnd.toZonedDateTime(),

                avgPM25 = entity.sumPm25.toDouble() / count,
                maxPM25 = entity.maxPm25,
                minPM25 = entity.minPm25,

                avgHumidity = entity.sumHumidity / count,
                avgTemperature = entity.sumTemperature / count,
                avgEco2PPM = entity.sumEco2 / count,
                avgBvocPPM = entity.sumBvoc / count,

                sampleCount = sampleCount,
            )

            return AirQualityViewDTO(
                timeUnit = timeUnit,
                content = content,
            )
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
    val sampleCount: Long,
)