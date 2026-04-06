package jnu.econovation.ecoknockbecentral.grpc.client.sensor

import jnu.econovation.ecoknockbecentral.grpc.config.EmbeddedGrpcConfig
import jnu.econovation.ecoknockbecentral.grpc.sensor.v1.GetCurrentSensorRequest
import jnu.econovation.ecoknockbecentral.grpc.sensor.v1.SensorServiceGrpc
import jnu.econovation.ecoknockbecentral.sensor.dto.internal.CurrentSensorDTO
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.concurrent.TimeUnit

@Component
class SensorGrpcClient(
    private val config: EmbeddedGrpcConfig,
    private val stub: SensorServiceGrpc.SensorServiceBlockingStub,
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
        )
    }
}
