package jnu.econovation.ecoknockbecentral.grpc.client.sensor

import jnu.econovation.ecoknockbecentral.grpc.config.EmbeddedGrpcConfig
import jnu.econovation.ecoknockbecentral.grpc.sensor.v2.GetCurrentSensorRequest
import jnu.econovation.ecoknockbecentral.grpc.sensor.v2.SensorServiceGrpc
import jnu.econovation.ecoknockbecentral.sensor.dto.internal.CurrentSensorDTO
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.concurrent.TimeUnit

@Component
class SensorGrpcClient(
    private val config: EmbeddedGrpcConfig,
    private val stub: SensorServiceGrpc.SensorServiceBlockingV2Stub,
) {
    suspend fun getCurrentSensor(): CurrentSensorDTO {
        val response = stub
            .withDeadlineAfter(config.timeout.toMillis(), TimeUnit.MILLISECONDS)
            .getCurrentSensor(GetCurrentSensorRequest.getDefaultInstance())

        return CurrentSensorDTO(
            temperatureC = response.temperatureC,
            humidityRh = response.humidityRh,
            gasResistanceOhm = response.gasResistanceOhm,
            status = response.status,
            gasValid = response.gasValid,
            heatStable = response.heatStable,
            measuredAt = Instant.ofEpochMilli(response.measuredAtUnixMs),
            staticIaq = response.staticIaq,
            accuracy = response.accuracy,
            estimatedBvocPPM = response.estimatedBvocPpm,
            estimatedEco2PPM = response.estimatedEco2Ppm,
            stabilizationProgressPercent = response.stabilizationProgressPct,
            gasPercentage = response.gasPercentage,
            learningCompleteAt = Instant.ofEpochMilli(response.learningCompleteAtUnixMs),
        )
    }
}
