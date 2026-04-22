package jnu.econovation.ecoknockbecentral.airquality.dto.request

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import jnu.econovation.ecoknockbecentral.airquality.exception.BadAirQualityResolutionException


enum class AirQualityResolution(
    @get:JsonValue
    val code: String,
) {
    ONE_MINUTE("1m"),
    FIVE_MINUTES("5m"),
    FIFTEEN_MINUTES("15m"),
    ONE_HOUR("1h"),
    FOUR_HOURS("4h"),
    ONE_DAY("1d");

    companion object {
        @JvmStatic
        fun supportedCodes(): String {
            return entries.joinToString(", ") { it.code }
        }

        fun from(code: String): AirQualityResolution? {
            return entries.find { it.code == code }
        }

        @JvmStatic
        @JsonCreator
        fun fromOrThrowBusinessException(code: String): AirQualityResolution {
            return from(code) ?: throw BadAirQualityResolutionException()
        }
    }
}
