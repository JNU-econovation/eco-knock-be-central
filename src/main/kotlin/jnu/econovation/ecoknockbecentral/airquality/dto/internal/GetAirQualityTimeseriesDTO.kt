package jnu.econovation.ecoknockbecentral.airquality.dto.internal

import jnu.econovation.ecoknockbecentral.airquality.readmodel.entity.AirQualityAggregate
import jnu.econovation.ecoknockbecentral.common.exception.server.InternalServerException
import jnu.econovation.ecoknockbecentral.common.extension.toZonedDateTime
import org.springframework.data.domain.Slice
import java.time.ZonedDateTime

typealias AirQualityTimeseriesSlice = Slice<AirQualityTimeseriesPointDTO>

data class AirQualityTimeseriesPointDTO(
    val time: ZonedDateTime,
    val end: ZonedDateTime,
    val pm25Quality: Quality,
    val humidity: Double,
    val temperature: Double,
    val gasQuality: Quality,
    val sampleCount: Long,
) {
    companion object {
        fun from(entity: AirQualityAggregate): AirQualityTimeseriesPointDTO {
            val sampleCount = entity.sampleCount

            if (sampleCount <= 0) {
                throw InternalServerException(
                    IllegalStateException(
                        "AirQualityAggregate sampleCount가 0 이하입니다. bucketStart=${entity.bucketStart}"
                    )
                )
            }

            val count = sampleCount.toDouble()

            val avgPm25 = entity.sumPm25.toDouble() / count
            val avgHumidity = entity.sumHumidity / count
            val avgTemperature = entity.sumTemperature / count
            val avgEco2 = entity.sumEco2 / count
            val avgBvoc = entity.sumBvoc / count

            return AirQualityTimeseriesPointDTO(
                time = entity.bucketStart.toZonedDateTime(),
                end = entity.bucketEnd.toZonedDateTime(),
                pm25Quality = Quality.fromPm25(avgPm25),
                humidity = avgHumidity,
                temperature = avgTemperature,
                gasQuality = Quality.fromGas(
                    eco2 = avgEco2,
                    bvoc = avgBvoc,
                ),
                sampleCount = sampleCount,
            )
        }
    }
}