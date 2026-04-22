package jnu.econovation.ecoknockbecentral.airquality.service

import jnu.econovation.ecoknockbecentral.airquality.command.SaveAirQualityCommand
import jnu.econovation.ecoknockbecentral.airquality.model.entity.AirQuality
import jnu.econovation.ecoknockbecentral.airquality.repository.AirQualityRepository
import jnu.econovation.ecoknockbecentral.airquality.usecase.SaveAirQualityUseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class AirQualityCommandService(
    private val repository: AirQualityRepository
) : SaveAirQualityUseCase {
    override fun save(command: SaveAirQualityCommand) {
        val airQualityDTO = command.airQuality
        val rawSensorDTO = command.rawSensor
        val rawAirPurifierDTO = command.rawAirPurifier

        val entity: AirQuality = AirQuality.builder()
            .humidity(airQualityDTO.humidity)
            .temperature(airQualityDTO.temperature)
            .airPurifierMeasuredAt(rawAirPurifierDTO.measuredAt)
            .sensorMeasuredAt(rawSensorDTO.measuredAt)
            .accuracy(airQualityDTO.accuracy)
            .estimatedBvocPPM(airQualityDTO.estimatedBvocPPM)
            .estimatedEco2PPM(airQualityDTO.estimatedEco2PPM)
            .pm25(airQualityDTO.pm25)
            .rawSensor(rawSensorDTO)
            .rawAirPurifier(rawAirPurifierDTO)
            .build()

        repository.save(entity)
    }
}
