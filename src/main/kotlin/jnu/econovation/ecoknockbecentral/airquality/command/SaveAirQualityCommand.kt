package jnu.econovation.ecoknockbecentral.airquality.command

import jnu.econovation.ecoknockbecentral.airquality.dto.AirQualityDTO
import jnu.econovation.ecoknockbecentral.airquality.dto.RawAirPurifierDTO
import jnu.econovation.ecoknockbecentral.airquality.dto.RawSensorDTO

data class SaveAirQualityCommand(
    val airQuality: AirQualityDTO,
    val rawSensor: RawSensorDTO,
    val rawAirPurifier: RawAirPurifierDTO
)