package jnu.econovation.ecoknockbecentral.sensor.dto.internal

import java.time.Instant

data class CurrentSensorDTO(
    val temperatureC: Double,
    val humidityRh: Double,
    val gasResistanceOhm: Double,
    val status: Int,
    val gasValid: Boolean,
    val heatStable: Boolean,
    val measuredAt: Instant,
    val staticIaq: Double,
    val estimatedEco2PPM: Double,
    val estimatedBvocPPM: Double,
    val accuracy: Int,
    val stabilizationProgressPercent: Int,
    val gasPercentage: Double,
    val learningCompleteAt: Instant
)
