package jnu.econovation.ecoknockbecentral.grpc.runner

import jakarta.annotation.PreDestroy
import jnu.econovation.ecoknockbecentral.grpc.client.airpurifier.AirPurifierGrpcClient
import jnu.econovation.ecoknockbecentral.grpc.client.sensor.SensorGrpcClient
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.time.delay
import mu.KLogging
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class EmbeddedGrpcStartupLogger(
    private val sensorGrpcClient: SensorGrpcClient,
    private val airPurifierGrpcClient: AirPurifierGrpcClient,
) : KLogging() {

    private var loggingJob: Job? = null

    private val loggerScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO + CoroutineExceptionHandler { _, throwable ->
            logger.error(throwable) { "gRPC Logger Scope에서 예외 포착" }
        }
    )

    companion object {
        private val POLLING_DELAY = Duration.ofSeconds(1)
    }

    @EventListener(ApplicationReadyEvent::class)
    fun logCurrentDeviceStatus() {
        loggingJob = loggerScope.launch {
            pollAndLog()
        }
    }

    @PreDestroy
    fun destroyJob() {
        logger.info { "logging job cancel 하는 중" }
        loggingJob?.cancel()
    }

    private suspend fun pollAndLog() = supervisorScope {
        while (true) {
            val sensorDeferred = async { runCatching { sensorGrpcClient.getCurrentSensor() } }
            val airPurifierDeferred = async { runCatching { airPurifierGrpcClient.getCurrentAirPurifier() } }

            sensorDeferred.await()
                .onSuccess { logger.info { "센서 -> $it" } }
                .onFailure { logger.error(it) { "센서 gRPC 조회 실패" } }

            airPurifierDeferred.await()
                .onSuccess { logger.info { "공기 청정기 -> $it" } }
                .onFailure { logger.error(it) { "공기청정기 gRPC 조회 실패" } }

            delay(POLLING_DELAY)
        }
    }
}
