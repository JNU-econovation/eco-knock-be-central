package jnu.econovation.ecoknockbecentral.airquality.scheduler

import jnu.econovation.ecoknockbecentral.airquality.service.AirQualityAggregateService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.core.env.MapPropertySource

class AirQualitySchedulerConditionTest {
    @Test
    fun createsSchedulerWhenEnabled() {
        schedulerContext(enabled = true).use { context ->
            assertThat(context.getBeansOfType(AirQualityScheduler::class.java)).hasSize(1)
        }
    }

    @Test
    fun doesNotCreateSchedulerWhenDisabled() {
        schedulerContext(enabled = false).use { context ->
            assertThat(context.getBeansOfType(AirQualityScheduler::class.java)).isEmpty()
        }
    }

    private fun schedulerContext(enabled: Boolean): AnnotationConfigApplicationContext {
        return AnnotationConfigApplicationContext().apply {
            environment.propertySources.addFirst(
                MapPropertySource(
                    "test",
                    mapOf("air-quality.scheduler.enabled" to enabled.toString()),
                )
            )
            beanFactory.registerSingleton("service", mock(AirQualityAggregateService::class.java))
            register(AirQualityScheduler::class.java)
            refresh()
        }
    }
}
