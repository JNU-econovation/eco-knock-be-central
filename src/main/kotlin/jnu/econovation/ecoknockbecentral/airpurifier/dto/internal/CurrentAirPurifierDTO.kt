package jnu.econovation.ecoknockbecentral.airpurifier.dto.internal

data class CurrentAirPurifierDTO(
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
)
