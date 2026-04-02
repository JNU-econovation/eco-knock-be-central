package jnu.econovation.ecoknockbecentral.grpc.health

import io.grpc.BindableService
import io.grpc.health.v1.HealthCheckResponse.ServingStatus
import io.grpc.protobuf.services.HealthStatusManager
import jnu.econovation.ecoknockbecentral.grpc.sensor.v1.SensorServiceGrpc
import org.springframework.stereotype.Component

@Component
class GrpcHealthService {
    private val healthStatusManager = HealthStatusManager()

    fun bindableService(): BindableService = healthStatusManager.healthService

    fun markServing() {
        healthStatusManager.setStatus("", ServingStatus.SERVING)
        healthStatusManager.setStatus(SensorServiceGrpc.SERVICE_NAME, ServingStatus.SERVING)
    }

    fun markNotServing() {
        healthStatusManager.enterTerminalState()
    }
}
