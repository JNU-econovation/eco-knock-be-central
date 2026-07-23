package jnu.econovation.ecoknockbecentral.airquality.model.vo

import com.fasterxml.jackson.databind.ObjectMapper
import jnu.econovation.ecoknockbecentral.airquality.converter.AirQualityResolutionConverter
import jnu.econovation.ecoknockbecentral.airquality.exception.BadAirQualityResolutionException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class AirQualityResolutionTest {
    private val objectMapper = ObjectMapper().findAndRegisterModules()
    private val converter = AirQualityResolutionConverter()

    @Test
    fun serializesAndDeserializesResolutionCode() {
        val serialized = objectMapper.writeValueAsString(AirQualityResolution.FIFTEEN_MINUTES)
        val deserialized = objectMapper.readValue(serialized, AirQualityResolution::class.java)

        assertThat(serialized).isEqualTo("\"15m\"")
        assertThat(deserialized).isEqualTo(AirQualityResolution.FIFTEEN_MINUTES)
    }

    @Test
    fun convertsQueryResolutionCode() {
        assertThat(converter.convert("1h")).isEqualTo(AirQualityResolution.ONE_HOUR)
    }

    @Test
    fun rejectsUnsupportedResolutionCode() {
        assertThatThrownBy { converter.convert("10m") }
            .isInstanceOf(BadAirQualityResolutionException::class.java)
    }
}
