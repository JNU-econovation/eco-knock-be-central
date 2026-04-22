package jnu.econovation.ecoknockbecentral.airquality.dto.response

import jnu.econovation.ecoknockbecentral.airquality.command.SaveAirQualityCommand
import jnu.econovation.ecoknockbecentral.common.toZonedDateTime
import java.time.ZonedDateTime

data class AirQualityRealtimeResponse(
    val measuredAt: ZonedDateTime,
    val pm25: Int,
    val humidity: Double,
    val temperature: Double,
    val estimatedEco2PPM: Double,
    val estimatedBvocPPM: Double,
    val accuracy: Int,
) {
    companion object {
        fun from(command: SaveAirQualityCommand): AirQualityRealtimeResponse {
            val airQuality = command.airQuality

            return AirQualityRealtimeResponse(
                measuredAt = command.rawSensor.measuredAt.toZonedDateTime(),
                pm25 = airQuality.pm25,
                humidity = airQuality.humidity,
                temperature = airQuality.temperature,
                estimatedEco2PPM = airQuality.estimatedEco2PPM,
                estimatedBvocPPM = airQuality.estimatedBvocPPM,
                accuracy = airQuality.accuracy,
            )
        }
    }
}