package jnu.econovation.ecoknockbecentral.airquality.dto

import java.time.Instant

data class RawAirPurifierDTO(
    val power: String,
    val isOn: Boolean,
    val aqi: Int,
    val averageAqi: Int,
    val humidity: Int,
    val temperatureC: Double?,
    val mode: String,
    val favoriteLevel: Int,
    val filterLifeRemaining: Int,
    val filterHoursUsed: Int,
    val motorSpeed: Int,
    val purifyVolume: Int,
    val led: Boolean,
    val ledBrightness: Int?,
    val buzzer: Boolean?,
    val childLock: Boolean,
    val measuredAt: Instant
)

data class RawSensorDTO(
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