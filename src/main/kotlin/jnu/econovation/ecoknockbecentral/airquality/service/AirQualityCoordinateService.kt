package jnu.econovation.ecoknockbecentral.airquality.service

import jnu.econovation.ecoknockbecentral.airquality.command.SaveAirQualityCommand
import jnu.econovation.ecoknockbecentral.airquality.model.entity.AirQuality
import jnu.econovation.ecoknockbecentral.airquality.repository.AirQualityRepository
import jnu.econovation.ecoknockbecentral.airquality.usecase.SaveAirQualityUseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AirQualityCoordinateService(
    private val repository: AirQualityRepository
) : SaveAirQualityUseCase {
    // TODO: 분 단위/시간 단위 집계 조회 요구가 커지면 읽기 모델(집계 테이블 또는 materialized view) 분리 및 CQRS 검토

    @Transactional
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
