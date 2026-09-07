package jnu.econovation.ecoknockbecentral.airquality.repository

import jnu.econovation.ecoknockbecentral.airquality.model.entity.AirQuality
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.Instant

interface AirQualityRepository : JpaRepository<AirQuality, Long> {
    @Query(
        value = """
            select aq
            from AirQuality aq
            where aq.sensorMeasuredAt >= :from
            and aq.sensorMeasuredAt < :to
            order by aq.sensorMeasuredAt
        """
    )
    fun findBetweenSensorMeasures(
        from: Instant,
        to: Instant
    ): List<AirQuality>
}