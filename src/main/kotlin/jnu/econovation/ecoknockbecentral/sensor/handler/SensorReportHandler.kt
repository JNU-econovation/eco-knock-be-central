package jnu.econovation.ecoknockbecentral.sensor.handler

import jnu.econovation.ecoknockbecentral.sensor.dto.internal.SensorReportDTO
import mu.KLogging
import org.springframework.stereotype.Component

@Component
class SensorReportHandler : KLogging() {
    fun handle(report: SensorReportDTO): Boolean {
        logger.info {
            "라즈베리파이 서버의 센서 Report를 받음 -> deviceId=${report.deviceId}," +
                    " temperatureC=${report.temperatureC}," +
                    " humidityRh=${report.humidityRh}," +
                    " gasResistanceOhm=${report.gasResistanceOhm}," +
                    " measuredAt=${report.measuredAt}"
        }
        return true
    }
}