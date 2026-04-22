package jnu.econovation.ecoknockbecentral.airquality.dto.response

import jnu.econovation.ecoknockbecentral.airquality.dto.AirQualityViewDTO

data class GetAirQualityResponse(val airQualities: List<AirQualityViewDTO>)