package jnu.econovation.ecoknockbecentral.airquality.controller

import jnu.econovation.ecoknockbecentral.airquality.dto.request.GetTimeseriesHistoryRequest
import jnu.econovation.ecoknockbecentral.airquality.dto.request.GetTimeseriesRequest
import jnu.econovation.ecoknockbecentral.airquality.dto.response.AirQualityTimeseriesPointResponse
import jnu.econovation.ecoknockbecentral.airquality.usecase.QueryAirQualityUseCase
import org.springframework.data.domain.Slice
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/air-quality/timeseries")
class AirQualityTimeseriesController(
    private val queryAirQualityUseCase: QueryAirQualityUseCase,
) {
    @GetMapping
    fun timeseries(@RequestBody request: GetTimeseriesRequest): Slice<AirQualityTimeseriesPointResponse> {
        return queryAirQualityUseCase.queryAirQualityTimeseries(request)
    }

    @GetMapping("/history")
    fun history(@RequestBody request: GetTimeseriesHistoryRequest): Slice<AirQualityTimeseriesPointResponse> {
        return queryAirQualityUseCase.queryAirQualityTimeseriesHistory(request)
    }
}
