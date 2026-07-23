package jnu.econovation.ecoknockbecentral.airquality.dto.rest.request

import io.swagger.v3.oas.annotations.media.Schema
import jnu.econovation.ecoknockbecentral.airquality.model.vo.AirQualityResolution

data class UpdateAirQualityHistorySettingRequest(
    @field:Schema(description = "과거 시계열 집계 해상도", example = "15m")
    val resolution: AirQualityResolution,
)
