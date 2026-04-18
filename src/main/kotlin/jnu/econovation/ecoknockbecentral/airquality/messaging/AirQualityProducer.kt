package jnu.econovation.ecoknockbecentral.airquality.messaging

import jakarta.annotation.PreDestroy
import jnu.econovation.ecoknockbecentral.airquality.command.SaveAirQualityCommand
import jnu.econovation.ecoknockbecentral.airquality.dto.AirQualityDTO
import jnu.econovation.ecoknockbecentral.airquality.dto.RawAirPurifierDTO
import jnu.econovation.ecoknockbecentral.airquality.dto.RawSensorDTO
import jnu.econovation.ecoknockbecentral.airquality.queue.SaveAirQualityQueue
import jnu.econovation.ecoknockbecentral.grpc.client.airpurifier.AirPurifierGrpcClient
import jnu.econovation.ecoknockbecentral.grpc.client.sensor.SensorGrpcClient
import kotlinx.coroutines.*
import kotlinx.coroutines.time.delay
import mu.KotlinLogging
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class AirQualityProducer(
    private val sensorGrpcClient: SensorGrpcClient,
    private val airPurifierGrpcClient: AirPurifierGrpcClient,
    private val queue: SaveAirQualityQueue
) {
    companion object {
        private val logger = KotlinLogging.logger {}
        private val POLLING_DELAY = Duration.ofSeconds(1)
        private val INITIAL_ERROR_DELAY = Duration.ofSeconds(30)
    }

    private val producerScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO + CoroutineExceptionHandler { _, throwable ->
            logger.error(throwable) { "gRPC 센서 producer Scope에서 예외 포착" }
        }
    )

    private var producingJob: Job? = null
    private var errorDelay = INITIAL_ERROR_DELAY


    @EventListener(ApplicationReadyEvent::class)
    fun start() {
        producingJob = producerScope.launch {
            pollAndProduce()
        }
    }

    @PreDestroy
    fun cancel() {
        logger.info { "producing job cancel 중" }
        producingJob?.cancel()
    }


    private suspend fun pollAndProduce() = supervisorScope {
        while (true) {
            val sensorDeferred = async {
                runCatching { sensorGrpcClient.getCurrentSensor() }
            }
            val airPurifierDeferred = async {
                runCatching { airPurifierGrpcClient.getCurrentAirPurifier() }
            }

            val sensor = sensorDeferred.await().getOrElse {
                logger.error(it) { "센서 gRPC 조회 실패" }
                null
            }
            val airPurifier = airPurifierDeferred.await().getOrElse {
                logger.error(it) { "공기청정기 gRPC 조회 실패" }
                null
            }

            logger.debug { "센서 gRPC -> $sensor" }
            logger.debug { "공기청정기 gRPC -> $airPurifier" }

            val failed = (sensor == null || airPurifier == null)
            if (failed) {
                logger.warn { "gRPC 에러로 인해 polling ${errorDelay.toSeconds()}초 딜레이" }
                delay(errorDelay)
                errorDelay = errorDelay.multipliedBy(2)

                continue
            }

            val airQuality: AirQualityDTO = merge(sensor, airPurifier)
            val command = SaveAirQualityCommand(
                airQuality = airQuality,
                rawSensor = sensor,
                rawAirPurifier = airPurifier
            )

            queue.enqueue(command)

            errorDelay = INITIAL_ERROR_DELAY

            delay(POLLING_DELAY)
        }
    }

    private fun merge(
        sensor: RawSensorDTO,
        airPurifier: RawAirPurifierDTO
    ): AirQualityDTO {
        val temperature = if (airPurifier.temperatureC == null) sensor.temperatureC
        else (airPurifier.temperatureC + sensor.temperatureC) / 2

        return AirQualityDTO(
            pm25 = airPurifier.averageAqi,
            humidity = (airPurifier.humidity + sensor.humidityRh) / 2,
            temperature = temperature,
            estimatedEco2PPM = sensor.estimatedEco2PPM,
            estimatedBvocPPM = sensor.estimatedBvocPPM,
            accuracy = sensor.accuracy,
        )
    }
}