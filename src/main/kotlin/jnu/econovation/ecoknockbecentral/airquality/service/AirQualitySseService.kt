package jnu.econovation.ecoknockbecentral.airquality.service

import jnu.econovation.ecoknockbecentral.airquality.command.SaveAirQualityCommand
import jnu.econovation.ecoknockbecentral.airquality.dto.response.AirQualityRealtimeResponse
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException
import java.util.concurrent.CopyOnWriteArrayList

@Service
class AirQualitySseService {
    companion object {
        private val logger = KotlinLogging.logger {}
        private const val TIMEOUT_MILLIS = 0L
        private const val EVENT_NAME = "air-quality"
    }

    private val emitters = CopyOnWriteArrayList<SseEmitter>()

    fun subscribe(): SseEmitter {
        val emitter = SseEmitter(TIMEOUT_MILLIS)

        emitters.add(emitter)
        emitter.onCompletion { emitters.remove(emitter) }
        emitter.onTimeout { emitters.remove(emitter) }
        emitter.onError { emitters.remove(emitter) }

        runCatching {
            emitter.send(SseEmitter.event().name("connected").data("ok"))
        }.onFailure {
            emitters.remove(emitter)
        }

        return emitter
    }

    fun publish(command: SaveAirQualityCommand) {
        val response = AirQualityRealtimeResponse.from(command)

        emitters.forEach { emitter ->
            runCatching {
                emitter.send(SseEmitter.event().name(EVENT_NAME).data(response))
            }.onFailure { throwable ->
                emitters.remove(emitter)
                if (throwable !is IOException) {
                    logger.warn(throwable) { "air quality SSE 전송 실패" }
                }
            }
        }
    }
}