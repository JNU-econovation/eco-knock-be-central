package jnu.econovation.ecoknockbecentral.sensor.dto.internal

import jnu.econovation.ecoknockbecentral.grpc.sensor.v1.ReportSensorRequest
import java.time.Instant

data class SensorReportDTO(
    val deviceId: Int,
    val temperatureC: Double,
    val humidityRh: Double,
    val gasResistanceOhm: Double,
    val measuredAt: Instant,
) {
    companion object {
        fun from(request: ReportSensorRequest) : SensorReportDTO {
            return SensorReportDTO(
                deviceId = request.deviceId,
                temperatureC = request.temperatureC,
                humidityRh = request.humidityRh,
                gasResistanceOhm = request.gasResistanceOhm,
                measuredAt = Instant.ofEpochMilli(request.measuredAtUnixMs)
            )
        }
    }
}