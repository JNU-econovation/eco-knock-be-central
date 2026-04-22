package jnu.econovation.ecoknockbecentral.airquality.usecase

import jnu.econovation.ecoknockbecentral.airquality.dto.request.GetTimeseriesHistoryRequest
import jnu.econovation.ecoknockbecentral.airquality.dto.request.GetTimeseriesRequest
import jnu.econovation.ecoknockbecentral.airquality.dto.response.AirQualityTimeseriesPointResponse
import jnu.econovation.ecoknockbecentral.airquality.dto.response.GetAirQualityResponse
import jnu.econovation.ecoknockbecentral.common.annotation.UseCase
import org.springframework.data.domain.Slice

@UseCase
interface QueryAirQualityUseCase {
    fun queryAirQuality() : GetAirQualityResponse

    fun queryAirQualityTimeseries(request: GetTimeseriesRequest): Slice<AirQualityTimeseriesPointResponse>

    fun queryAirQualityTimeseriesHistory(request: GetTimeseriesHistoryRequest): Slice<AirQualityTimeseriesPointResponse>
}
