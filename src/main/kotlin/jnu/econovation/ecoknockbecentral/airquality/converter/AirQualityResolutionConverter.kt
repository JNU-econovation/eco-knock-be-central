package jnu.econovation.ecoknockbecentral.airquality.converter

import jnu.econovation.ecoknockbecentral.airquality.dto.request.AirQualityResolution
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class AirQualityResolutionConverter : Converter<String, AirQualityResolution> {
    override fun convert(source: String): AirQualityResolution {
        return AirQualityResolution.fromOrThrowBusinessException(source)
    }
}