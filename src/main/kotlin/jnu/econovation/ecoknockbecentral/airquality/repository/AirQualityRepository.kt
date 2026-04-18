package jnu.econovation.ecoknockbecentral.airquality.repository

import jnu.econovation.ecoknockbecentral.airquality.model.entity.AirQuality
import org.springframework.data.jpa.repository.JpaRepository

interface AirQualityRepository : JpaRepository<AirQuality, Long> {
}