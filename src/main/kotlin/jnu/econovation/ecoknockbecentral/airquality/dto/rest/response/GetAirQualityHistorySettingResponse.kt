package jnu.econovation.ecoknockbecentral.airquality.dto.rest.response

import jnu.econovation.ecoknockbecentral.airquality.model.entity.AirQualityHistorySetting
import jnu.econovation.ecoknockbecentral.airquality.model.vo.AirQualityResolution

data class GetAirQualityHistorySettingResponse(
    val resolution: AirQualityResolution,
) {
    companion object {
        fun from(setting: AirQualityHistorySetting): GetAirQualityHistorySettingResponse {
            return GetAirQualityHistorySettingResponse(setting.resolution)
        }
    }
}
