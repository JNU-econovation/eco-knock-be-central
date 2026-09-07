package jnu.econovation.ecoknockbecentral.airquality.scheduler

import jnu.econovation.ecoknockbecentral.airquality.service.AirQualityAggregateService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
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
    private val service: AirQualityAggregateService
) {
    @Scheduled(cron = "5 * * * * *")
    fun aggregateAirQuality() {
        service.execute()
    }
}
