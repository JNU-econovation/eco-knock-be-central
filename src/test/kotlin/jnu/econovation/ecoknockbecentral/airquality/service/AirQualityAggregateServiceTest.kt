package jnu.econovation.ecoknockbecentral.airquality.service

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import jnu.econovation.ecoknockbecentral.airquality.model.entity.AirQuality
import jnu.econovation.ecoknockbecentral.airquality.readmodel.entity.AirQuality1mAggregate
import jnu.econovation.ecoknockbecentral.airquality.readmodel.entity.AirQuality5mAggregate
import jnu.econovation.ecoknockbecentral.airquality.repository.AirQuality1dAggregateRepository
import jnu.econovation.ecoknockbecentral.airquality.repository.AirQuality1hAggregateRepository
import jnu.econovation.ecoknockbecentral.airquality.repository.AirQuality1mAggregateRepository
import jnu.econovation.ecoknockbecentral.airquality.repository.AirQuality4hAggregateRepository
import jnu.econovation.ecoknockbecentral.airquality.repository.AirQuality5mAggregateRepository
import jnu.econovation.ecoknockbecentral.airquality.repository.AirQuality15mAggregateRepository
import jnu.econovation.ecoknockbecentral.airquality.repository.AirQualityRepository
import jnu.econovation.ecoknockbecentral.common.metrics.ApplicationMetrics
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.Optional

class AirQualityAggregateServiceTest {
    private val airQualityRepository = mock<AirQualityRepository>()
    private val oneMinuteRepository = mock<AirQuality1mAggregateRepository>()
    private val fiveMinuteRepository = mock<AirQuality5mAggregateRepository>()
    private val fifteenMinuteRepository = mock<AirQuality15mAggregateRepository>()
    private val oneHourRepository = mock<AirQuality1hAggregateRepository>()
    private val fourHourRepository = mock<AirQuality4hAggregateRepository>()
    private val oneDayRepository = mock<AirQuality1dAggregateRepository>()
    private val clock = Clock.fixed(Instant.parse("2026-01-01T13:38:57Z"), ZoneOffset.UTC)
    private val metrics = ApplicationMetrics(SimpleMeterRegistry())
    private val service = AirQualityAggregateService(
        airQualityRepository,
        oneMinuteRepository,
        fiveMinuteRepository,
        fifteenMinuteRepository,
        oneHourRepository,
        fourHourRepository,
        oneDayRepository,
        clock,
        metrics,
    )

    @BeforeEach
    fun setUp() {
        whenever(oneMinuteRepository.findBuckets(any(), any())).thenReturn(emptyList())
        whenever(oneMinuteRepository.findById(any())).thenReturn(Optional.empty())
        whenever(fiveMinuteRepository.findById(any())).thenReturn(Optional.empty())
        whenever(fifteenMinuteRepository.findById(any())).thenReturn(Optional.empty())
        whenever(oneHourRepository.findById(any())).thenReturn(Optional.empty())
        whenever(fourHourRepository.findById(any())).thenReturn(Optional.empty())
        whenever(oneDayRepository.findById(any())).thenReturn(Optional.empty())
    }

    @Test
    fun aggregatesOnlyTheMostRecentThreeMinutesIntoOneMinuteBuckets() {
        val from = Instant.parse("2026-01-01T13:35:00Z")
        val to = Instant.parse("2026-01-01T13:38:00Z")
        whenever(airQualityRepository.findBetweenSensorMeasures(from, to)).thenReturn(
            listOf(
                airQuality("2026-01-01T13:35:10Z", 10, 40.0, 20.0, 500.0, 0.1),
                airQuality("2026-01-01T13:35:50Z", 20, 60.0, 22.0, 700.0, 0.3),
                airQuality("2026-01-01T13:37:10Z", 30, 50.0, 24.0, 900.0, 0.5),
            )
        )

        service.execute()

        verify(airQualityRepository).findBetweenSensorMeasures(from, to)
        val aggregates = argumentCaptor<AirQuality1mAggregate>()
        verify(oneMinuteRepository, org.mockito.kotlin.times(2)).save(aggregates.capture())

        assertThat(aggregates.allValues.map { it.bucketStart })
            .containsExactly(
                Instant.parse("2026-01-01T13:35:00Z"),
                Instant.parse("2026-01-01T13:37:00Z"),
            )
        assertThat(aggregates.firstValue.sumPm25).isEqualTo(30L)
        assertThat(aggregates.firstValue.maxPm25).isEqualTo(20)
        assertThat(aggregates.firstValue.minPm25).isEqualTo(10)
        assertThat(aggregates.firstValue.sampleCount).isEqualTo(2L)
    }

    @Test
    fun replacesAnExistingOneMinuteBucketWithoutSavingAnotherEntity() {
        val bucketStart = Instant.parse("2026-01-01T13:35:00Z")
        val existing = oneMinuteAggregate(bucketStart, 1L, 1, 1, 1.0, 1.0, 1.0, 1.0, 1L)
        whenever(oneMinuteRepository.findById(bucketStart)).thenReturn(Optional.of(existing))
        whenever(airQualityRepository.findBetweenSensorMeasures(any(), any())).thenReturn(
            listOf(
                airQuality("2026-01-01T13:35:10Z", 10, 40.0, 20.0, 500.0, 0.1),
                airQuality("2026-01-01T13:35:50Z", 20, 60.0, 22.0, 700.0, 0.3),
            )
        )

        service.execute()

        verify(oneMinuteRepository, never()).save(any())
        assertThat(existing.sumPm25).isEqualTo(30L)
        assertThat(existing.maxPm25).isEqualTo(20)
        assertThat(existing.minPm25).isEqualTo(10)
        assertThat(existing.sampleCount).isEqualTo(2L)
    }

    @Test
    fun mergesOneMinuteBucketsIntoHigherResolutionAggregates() {
        whenever(airQualityRepository.findBetweenSensorMeasures(any(), any())).thenReturn(emptyList())
        whenever(oneMinuteRepository.findBuckets(any(), any())).thenReturn(
            listOf(
                oneMinuteAggregate(Instant.parse("2026-01-01T13:35:00Z"), 10L, 10, 10, 40.0, 20.0, 500.0, 0.1, 1L),
                oneMinuteAggregate(Instant.parse("2026-01-01T13:36:00Z"), 20L, 20, 20, 60.0, 22.0, 700.0, 0.3, 2L),
            )
        )

        service.execute()

        val fiveMinuteAggregate = argumentCaptor<AirQuality5mAggregate>()
        verify(fiveMinuteRepository).save(fiveMinuteAggregate.capture())
        assertThat(fiveMinuteAggregate.firstValue.bucketStart).isEqualTo(Instant.parse("2026-01-01T13:35:00Z"))
        assertThat(fiveMinuteAggregate.firstValue.sumPm25).isEqualTo(30L)
        assertThat(fiveMinuteAggregate.firstValue.maxPm25).isEqualTo(20)
        assertThat(fiveMinuteAggregate.firstValue.minPm25).isEqualTo(10)
        assertThat(fiveMinuteAggregate.firstValue.sumHumidity).isEqualTo(100.0)
        assertThat(fiveMinuteAggregate.firstValue.sampleCount).isEqualTo(3L)
    }

    private fun airQuality(
        measuredAt: String,
        pm25: Int,
        humidity: Double,
        temperature: Double,
        eco2: Double,
        bvoc: Double,
    ): AirQuality {
        val timestamp = Instant.parse(measuredAt)
        return AirQuality.builder()
            .sensorMeasuredAt(timestamp)
            .airPurifierMeasuredAt(timestamp)
            .pm25(pm25)
            .humidity(humidity)
            .temperature(temperature)
            .estimatedEco2PPM(eco2)
            .estimatedBvocPPM(bvoc)
            .accuracy(1)
            .build()
    }

    private fun oneMinuteAggregate(
        bucketStart: Instant,
        sumPm25: Long,
        maxPm25: Int,
        minPm25: Int,
        sumHumidity: Double,
        sumTemperature: Double,
        sumEco2: Double,
        sumBvoc: Double,
        sampleCount: Long,
    ): AirQuality1mAggregate {
        return AirQuality1mAggregate.builder()
            .bucketStart(bucketStart)
            .bucketEnd(bucketStart.plusSeconds(60))
            .sumPm25(sumPm25)
            .maxPm25(maxPm25)
            .minPm25(minPm25)
            .sumHumidity(sumHumidity)
            .sumTemperature(sumTemperature)
            .sumEco2(sumEco2)
            .sumBvoc(sumBvoc)
            .sampleCount(sampleCount)
            .build()
    }
}
