package jnu.econovation.ecoknockbecentral.grpc.client.airpurifier

import jnu.econovation.ecoknockbecentral.airquality.dto.RawAirPurifierDTO
import jnu.econovation.ecoknockbecentral.grpc.airpurifier.v1.AirPurifierServiceGrpc
import jnu.econovation.ecoknockbecentral.grpc.airpurifier.v1.GetCurrentAirPurifierRequest
import jnu.econovation.ecoknockbecentral.grpc.config.EmbeddedGrpcConfig
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.concurrent.TimeUnit

@Component
class AirPurifierGrpcClient(
    private val config: EmbeddedGrpcConfig,
    private val stub: AirPurifierServiceGrpc.AirPurifierServiceBlockingStub,
) {
    suspend fun getCurrentAirPurifier(): RawAirPurifierDTO {
        val response = stub
            .withDeadlineAfter(config.timeout.toMillis(), TimeUnit.MILLISECONDS)
            .getCurrentAirPurifier(GetCurrentAirPurifierRequest.getDefaultInstance())

        return RawAirPurifierDTO(
            power = response.power,
            isOn = response.isOn,
            aqi = response.aqi,
            averageAqi = response.averageAqi,
            humidity = response.humidity,
            temperatureC = if (response.hasTemperatureC()) response.temperatureC.value else null,
            mode = response.mode,
            favoriteLevel = response.favoriteLevel,
            filterLifeRemaining = response.filterLifeRemaining,
            filterHoursUsed = response.filterHoursUsed,
            motorSpeed = response.motorSpeed,
            purifyVolume = response.purifyVolume,
            led = response.led,
            ledBrightness = if (response.hasLedBrightness()) response.ledBrightness.value else null,
            buzzer = if (response.hasBuzzer()) response.buzzer.value else null,
            childLock = response.childLock,
            measuredAt = Instant.ofEpochMilli(response.measuredAtUnixMs),
        )
    }
}
