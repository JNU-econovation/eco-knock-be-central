package jnu.econovation.ecoknockbecentral.airquality.service

import jnu.econovation.ecoknockbecentral.airquality.model.entity.AirQuality
import jnu.econovation.ecoknockbecentral.airquality.readmodel.entity.*
import jnu.econovation.ecoknockbecentral.airquality.repository.*
import jnu.econovation.ecoknockbecentral.common.metrics.ApplicationMetrics
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

private data class AggregateValue(
    val sumPm25: Long,
    val maxPm25: Int,
    val minPm25: Int,
    val sumHumidity: Double,
    val sumTemperature: Double,
    val sumEco2: Double,
    val sumBvoc: Double,
    val sampleCount: Long,
)

@Service
@Transactional
class AirQualityAggregateService(
    private val airQualityRepository: AirQualityRepository,
    private val oneMinuteRepository: AirQuality1mAggregateRepository,
    private val fiveMinuteRepository: AirQuality5mAggregateRepository,
    private val fifteenMinuteRepository: AirQuality15mAggregateRepository,
    private val oneHourRepository: AirQuality1hAggregateRepository,
    private val fourHourRepository: AirQuality4hAggregateRepository,
    private val oneDayRepository: AirQuality1dAggregateRepository,
    private val clock: Clock,
    private val metrics: ApplicationMetrics,
) {

    companion object {
        private const val OVERLAP_DELAY = 3L
    }

    fun execute() {
        val to = clock.instant()
            .truncatedTo(ChronoUnit.MINUTES)

        val from = to.minus(
            OVERLAP_DELAY,
            ChronoUnit.MINUTES,
        )

        // raw → 1m
        metrics.recordAirQualityAggregation("1m") {
            executeOneMinute(from, to)
        }

        // 1m → 5m / 15m / 1h / 4h / 1d
        executeHigherAggregates(from, to)
    }

    /**
     * Raw AirQuality 데이터를 1분 단위로 집계한다.
     */
    private fun executeOneMinute(
        from: Instant,
        to: Instant,
    ) {
        val airQualities =
            airQualityRepository.findBetweenSensorMeasures(from, to)

        /*
         * 현재 시간이 13:38:57이고,
         * 집계 범위가 13:35:00 ~ 13:38:00이라면
         *
         * 13:35:20 -> 13:35:00 bucket
         * 13:35:59 -> 13:35:00 bucket
         * 13:36:10 -> 13:36:00 bucket
         * 13:37:42 -> 13:37:00 bucket
         *
         * 13:38:00 이후 데이터는 이번 집계 대상에서 제외된다.
         */
        val buckets: Map<Instant, List<AirQuality>> = airQualities.groupBy {
            it.sensorMeasuredAt.truncatedTo(ChronoUnit.MINUTES)
        }

        buckets.forEach { (start, values) ->
            val end = start.plus(1, ChronoUnit.MINUTES)
            val aggregateValue = calculateRaw(values)

            saveOrReplace(
                repository = oneMinuteRepository,
                bucketStart = start,
                bucketEnd = end,
                value = aggregateValue,
            ) { bucketStart, bucketEnd, value ->

                AirQuality1mAggregate.builder()
                    .bucketStart(bucketStart)
                    .bucketEnd(bucketEnd)
                    .sumPm25(value.sumPm25)
                    .maxPm25(value.maxPm25)
                    .minPm25(value.minPm25)
                    .sumHumidity(value.sumHumidity)
                    .sumTemperature(value.sumTemperature)
                    .sumEco2(value.sumEco2)
                    .sumBvoc(value.sumBvoc)
                    .sampleCount(value.sampleCount)
                    .build()
            }
        }
    }

    /**
     * 1분 Aggregate를 기반으로 상위 resolution Aggregate를 만든다.
     */
    private fun executeHigherAggregates(
        from: Instant,
        to: Instant,
    ) {
        metrics.recordAirQualityAggregation("5m") {
            executeFromOneMinute(
                from = from,
                to = to,
                bucketSize = Duration.ofMinutes(5),
                repository = fiveMinuteRepository,
            ) { start, end, value ->
                AirQuality5mAggregate.builder()
                    .bucketStart(start)
                    .bucketEnd(end)
                    .sumPm25(value.sumPm25)
                    .maxPm25(value.maxPm25)
                    .minPm25(value.minPm25)
                    .sumHumidity(value.sumHumidity)
                    .sumTemperature(value.sumTemperature)
                    .sumEco2(value.sumEco2)
                    .sumBvoc(value.sumBvoc)
                    .sampleCount(value.sampleCount)
                    .build()
            }
        }

        metrics.recordAirQualityAggregation("15m") {
            executeFromOneMinute(
                from = from,
                to = to,
                bucketSize = Duration.ofMinutes(15),
                repository = fifteenMinuteRepository,
            ) { start, end, value ->
                AirQuality15mAggregate.builder()
                    .bucketStart(start)
                    .bucketEnd(end)
                    .sumPm25(value.sumPm25)
                    .maxPm25(value.maxPm25)
                    .minPm25(value.minPm25)
                    .sumHumidity(value.sumHumidity)
                    .sumTemperature(value.sumTemperature)
                    .sumEco2(value.sumEco2)
                    .sumBvoc(value.sumBvoc)
                    .sampleCount(value.sampleCount)
                    .build()
            }
        }

        metrics.recordAirQualityAggregation("1h") {
            executeFromOneMinute(
                from = from,
                to = to,
                bucketSize = Duration.ofHours(1),
                repository = oneHourRepository,
            ) { start, end, value ->
                AirQuality1hAggregate.builder()
                    .bucketStart(start)
                    .bucketEnd(end)
                    .sumPm25(value.sumPm25)
                    .maxPm25(value.maxPm25)
                    .minPm25(value.minPm25)
                    .sumHumidity(value.sumHumidity)
                    .sumTemperature(value.sumTemperature)
                    .sumEco2(value.sumEco2)
                    .sumBvoc(value.sumBvoc)
                    .sampleCount(value.sampleCount)
                    .build()
            }
        }

        metrics.recordAirQualityAggregation("4h") {
            executeFromOneMinute(
                from = from,
                to = to,
                bucketSize = Duration.ofHours(4),
                repository = fourHourRepository,
            ) { start, end, value ->
                AirQuality4hAggregate.builder()
                    .bucketStart(start)
                    .bucketEnd(end)
                    .sumPm25(value.sumPm25)
                    .maxPm25(value.maxPm25)
                    .minPm25(value.minPm25)
                    .sumHumidity(value.sumHumidity)
                    .sumTemperature(value.sumTemperature)
                    .sumEco2(value.sumEco2)
                    .sumBvoc(value.sumBvoc)
                    .sampleCount(value.sampleCount)
                    .build()
            }
        }

        metrics.recordAirQualityAggregation("1d") {
            executeFromOneMinute(
                from = from,
                to = to,
                bucketSize = Duration.ofDays(1),
                repository = oneDayRepository,
            ) { start, end, value ->
                AirQuality1dAggregate.builder()
                    .bucketStart(start)
                    .bucketEnd(end)
                    .sumPm25(value.sumPm25)
                    .maxPm25(value.maxPm25)
                    .minPm25(value.minPm25)
                    .sumHumidity(value.sumHumidity)
                    .sumTemperature(value.sumTemperature)
                    .sumEco2(value.sumEco2)
                    .sumBvoc(value.sumBvoc)
                    .sampleCount(value.sampleCount)
                    .build()
            }
        }
    }

    /**
     * 1분 Aggregate 여러 개를 묶어서 상위 resolution을 만든다.
     *
     * 예:
     *
     * 13:35
     * 13:36
     * 13:37
     * 13:38
     * 13:39
     *
     * ↓
     *
     * 13:35 ~ 13:40 5분 Aggregate
     */
    private fun <T : AirQualityAggregate> executeFromOneMinute(
        from: Instant,
        to: Instant,
        bucketSize: Duration,
        repository: AirQualityAggregateRepository<T>,
        create: (Instant, Instant, AggregateValue) -> T,
    ) {
        /*
         * from 자체가 상위 bucket 중간에 있을 수 있기 때문에
         * 해당 bucket의 시작점까지 범위를 넓힌다.
         *
         * 예:
         * from = 13:37
         * bucketSize = 15분
         *
         * 실제 다시 계산해야 하는 시작점 = 13:30
         */
        val aggregateFrom = floorToBucket(
            from,
            bucketSize,
        )

        val oneMinuteAggregates = oneMinuteRepository.findBuckets(
            aggregateFrom,
            to,
        )

        val buckets = oneMinuteAggregates.groupBy {
            floorToBucket(it.bucketStart, bucketSize)
        }

        buckets.forEach { (start, values) ->
            val end = start.plus(bucketSize)
            val aggregateValue = merge(values)

            saveOrReplace(
                repository = repository,
                bucketStart = start,
                bucketEnd = end,
                value = aggregateValue,
                create = create,
            )
        }
    }

    /**
     * Raw 센서 데이터 → AggregateValue
     */
    private fun calculateRaw(
        airQualities: List<AirQuality>,
    ): AggregateValue {
        return AggregateValue(
            sumPm25 = airQualities.sumOf { it.pm25.toLong() },
            maxPm25 = airQualities.maxOf { it.pm25 },
            minPm25 = airQualities.minOf { it.pm25 },
            sumHumidity = airQualities.sumOf { it.humidity },
            sumTemperature = airQualities.sumOf { it.temperature },
            sumEco2 = airQualities.sumOf { it.estimatedEco2PPM },
            sumBvoc = airQualities.sumOf { it.estimatedBvocPPM },
            sampleCount = airQualities.size.toLong(),
        )
    }

    /**
     * 여러 1분 Aggregate를 합쳐 상위 AggregateValue를 만든다.
     *
     * 평균값을 평균내는 게 아니라
     * sum과 sampleCount를 그대로 합친다.
     */
    private fun merge(
        aggregates: List<AirQuality1mAggregate>,
    ): AggregateValue {
        return AggregateValue(
            sumPm25 = aggregates.sumOf { it.sumPm25 },
            maxPm25 = aggregates.maxOf { it.maxPm25 },
            minPm25 = aggregates.minOf { it.minPm25 },
            sumHumidity = aggregates.sumOf { it.sumHumidity },
            sumTemperature = aggregates.sumOf { it.sumTemperature },
            sumEco2 = aggregates.sumOf { it.sumEco2 },
            sumBvoc = aggregates.sumOf { it.sumBvoc },
            sampleCount = aggregates.sumOf { it.sampleCount },
        )
    }

    /**
     * 동일 bucket이 이미 존재하면 최신 재집계 결과로 교체하고,
     * 존재하지 않으면 새 row를 저장한다.
     */
    private fun <T : AirQualityAggregate> saveOrReplace(
        repository: AirQualityAggregateRepository<T>,
        bucketStart: Instant,
        bucketEnd: Instant,
        value: AggregateValue,
        create: (Instant, Instant, AggregateValue) -> T,
    ) {
        val existing = repository.findById(bucketStart).orElse(null)

        if (existing == null) {
            repository.save(create(bucketStart, bucketEnd, value))

            return
        }

        /*
         * @Transactional 안에서 조회된 Entity이므로
         * 별도의 save() 없이 Dirty Checking으로 UPDATE된다.
         */
        existing.replace(
            value.sumPm25,
            value.maxPm25,
            value.minPm25,
            value.sumHumidity,
            value.sumTemperature,
            value.sumEco2,
            value.sumBvoc,
            value.sampleCount,
        )
    }

    /**
     * 주어진 시각을 해당 resolution bucket의 시작점으로 내린다.
     *
     * 13:37 + 5분  → 13:35
     * 13:37 + 15분 → 13:30
     * 13:37 + 1시간 → 13:00
     * 13:37 + 4시간 → 12:00
     */
    private fun floorToBucket(
        instant: Instant,
        bucketSize: Duration,
    ): Instant {
        val bucketSeconds = bucketSize.seconds

        val bucketStartEpochSecond = Math.floorDiv(
            instant.epochSecond,
            bucketSeconds,
        ) * bucketSeconds

        return Instant.ofEpochSecond(
            bucketStartEpochSecond,
        )
    }
}
