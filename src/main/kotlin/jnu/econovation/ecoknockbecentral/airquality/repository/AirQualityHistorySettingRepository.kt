package jnu.econovation.ecoknockbecentral.airquality.repository

import jnu.econovation.ecoknockbecentral.airquality.model.entity.AirQualityHistorySetting
import org.springframework.data.jpa.repository.JpaRepository

interface AirQualityHistorySettingRepository : JpaRepository<AirQualityHistorySetting, Long> {
    fun findByMemberId(memberId: Long): AirQualityHistorySetting?

    fun existsByMemberId(memberId: Long): Boolean
}
