package jnu.econovation.ecoknockbecentral.airquality.controller

import jnu.econovation.ecoknockbecentral.airquality.service.AirQualitySseService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
@RequestMapping("/air-quality")
class AirQualitySseController(
    private val airQualitySseService: AirQualitySseService,
) {
    @GetMapping("/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun stream(): SseEmitter {
        return airQualitySseService.subscribe()
    }
}
