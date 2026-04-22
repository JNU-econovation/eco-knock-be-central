package jnu.econovation.ecoknockbecentral.airquality.scheduler

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class AirQualityScheduler(
    private val jdbcTemplate: JdbcTemplate
) {
    companion object {
        private val MATERIALIZED_VIEWS = listOf(
            "air_quality_1m_mv",
            "air_quality_5m_mv",
            "air_quality_15m_mv",
            "air_quality_1h_mv",
            "air_quality_4h_mv",
            "air_quality_1d_mv",
        )
    }

    @Scheduled(cron = "0 * * * * *")
    @Suppress("SqlSourceToSinkFlow")
    fun refreshAirQualityMaterializedViews() {
        MATERIALIZED_VIEWS.forEach { viewName ->
            jdbcTemplate.execute("refresh materialized view concurrently $viewName")
        }
    }
}