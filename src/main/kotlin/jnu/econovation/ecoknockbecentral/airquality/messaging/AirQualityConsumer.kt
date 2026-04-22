package jnu.econovation.ecoknockbecentral.airquality.messaging

import jakarta.annotation.PreDestroy
import jnu.econovation.ecoknockbecentral.airquality.queue.SaveAirQualityQueue
import jnu.econovation.ecoknockbecentral.airquality.service.AirQualitySseService
import jnu.econovation.ecoknockbecentral.airquality.usecase.SaveAirQualityUseCase
import kotlinx.coroutines.*
import mu.KotlinLogging
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
class AirQualityConsumer(
    private val queue: SaveAirQualityQueue,
    private val saveAirQualityUseCase: SaveAirQualityUseCase,
    private val airQualitySseService: AirQualitySseService,
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private var consumingJob: Job? = null

    private val consumerScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO + CoroutineExceptionHandler { _, throwable ->
            logger.error(throwable) { "gRPC 센서 consumer Scope에서 예외 포착" }
        }
    )

    @EventListener(ApplicationReadyEvent::class)
    fun start() {
        consumingJob = consumerScope.launch { consume() }
    }

    @PreDestroy
    fun cancel() {
        logger.info { "consuming job cancel 중" }
        consumingJob?.cancel()
    }

    private suspend fun consume() {
        queue.asFlow().collect { command ->
            runCatching {
                saveAirQualityUseCase.save(command)
                airQualitySseService.publish(command)
            }.onFailure { throwable ->
                logger.error(throwable) { "air quality save 실패" }
            }
        }
    }
}
