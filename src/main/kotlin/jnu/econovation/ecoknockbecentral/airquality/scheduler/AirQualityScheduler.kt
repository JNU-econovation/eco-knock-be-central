package jnu.econovation.ecoknockbecentral.airquality.scheduler

import jnu.econovation.ecoknockbecentral.common.metrics.ApplicationMetrics
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(
    prefix = "air-quality.scheduler",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = true,
)
class AirQualityScheduler(
    private val jdbcTemplate: JdbcTemplate,
    private val metrics: ApplicationMetrics,
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
            metrics.recordMaterializedViewRefresh(viewName) {
                jdbcTemplate.execute("refresh materialized view concurrently $viewName")
            }
        }
    }
}
