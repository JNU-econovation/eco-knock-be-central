package jnu.econovation.ecoknockbecentral.airquality.performance

import com.fasterxml.jackson.databind.ObjectMapper
import jnu.econovation.ecoknockbecentral.EcoKnockBeCentralApplication
import jnu.econovation.ecoknockbecentral.airquality.dto.internal.AirQualityTimeseriesPointDTO
import jnu.econovation.ecoknockbecentral.airquality.dto.internal.GetTimeseriesDTO
import jnu.econovation.ecoknockbecentral.airquality.dto.internal.GetTimeseriesHistoryDTO
import jnu.econovation.ecoknockbecentral.airquality.dto.internal.Quality
import jnu.econovation.ecoknockbecentral.airquality.model.vo.AirQualityResolution
import jnu.econovation.ecoknockbecentral.airquality.dto.rest.response.GetAirQualityResponse
import jnu.econovation.ecoknockbecentral.airquality.service.AirQualityQueryService
import jnu.econovation.ecoknockbecentral.airquality.usecase.QueryAirQualityUseCase
import jnu.econovation.ecoknockbecentral.common.extension.toZonedDateTime
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.PreparedStatementSetter
import org.springframework.jdbc.core.RowMapper
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.context.TestPropertySource
import org.springframework.web.client.RestClient
import java.nio.charset.StandardCharsets
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.Instant
import java.time.OffsetDateTime
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

@SpringBootTest(
    classes = [EcoKnockBeCentralApplication::class, AirQualityQueryPerformanceTest.TestConfig::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@TestPropertySource(properties = ["spring.task.scheduling.enabled=false"])
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@ExtendWith(SpringExtension::class)
@Timeout(value = 15, unit = TimeUnit.MINUTES)
class AirQualityQueryPerformanceTest(
    @param:LocalServerPort
    private val port: Int,
    private val jdbcTemplate: JdbcTemplate,
    private val mapper: ObjectMapper,
    private val benchmarkQueryUseCase: BenchmarkQueryUseCase,
) {
    private companion object {
        private val TEST_FROM = Instant.parse("2099-01-01T00:00:00Z")
        private val TEST_TO = Instant.parse("2099-01-31T00:00:00Z")
        private val TEST_FROM_TIMESTAMP = Timestamp.from(TEST_FROM)
        private val TEST_TO_TIMESTAMP = Timestamp.from(TEST_TO)

        private const val WARMUP_COUNT = 3
        private const val MEASUREMENT_COUNT = 10

        private val MATERIALIZED_VIEWS = listOf(
            "air_quality_1m_mv",
            "air_quality_5m_mv",
            "air_quality_15m_mv",
            "air_quality_1h_mv",
            "air_quality_4h_mv",
            "air_quality_1d_mv",
        )

        private const val INSERT_BENCHMARK_DATA_SQL = """
            insert into air_quality (
                sensor_measured_at,
                air_purifier_measured_at,
                pm25,
                humidity,
                temperature,
                estimated_eco2ppm,
                estimated_bvocppm,
                accuracy
            )
            select
                measured_at,
                measured_at,
                ((extract(epoch from measured_at)::bigint % 100) + 1)::integer,
                40.0 + (extract(epoch from measured_at)::bigint % 20)::double precision,
                20.0 + (extract(epoch from measured_at)::bigint % 10)::double precision,
                500.0 + (extract(epoch from measured_at)::bigint % 500)::double precision,
                0.1 + (extract(epoch from measured_at)::bigint % 10)::double precision / 10.0,
                1
            from generate_series(
                ?::timestamptz,
                ?::timestamptz - interval '1 second',
                interval '1 second'
            ) as generated(measured_at)
            """
    }

    private val restClient = RestClient.builder()
        .baseUrl("http://localhost:$port")
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build()

    @BeforeEach
    fun setUp() {
        deleteBenchmarkData()
        refreshMaterializedViews()

        jdbcTemplate.update(
            INSERT_BENCHMARK_DATA_SQL,
            TEST_FROM_TIMESTAMP,
            TEST_TO_TIMESTAMP,
        )
        refreshMaterializedViews()
    }

    @AfterEach
    fun tearDown() {
        benchmarkQueryUseCase.mode = QueryMode.MATERIALIZED_VIEW
        deleteBenchmarkData()
        refreshMaterializedViews()
    }

    @Test
    @DisplayName("materialized view와 원본 직접 집계 API의 성능을 비교한다")
    fun comparesMaterializedViewAndRawAggregationPerformance() {
        val results = AirQualityResolution.entries.associateWith { resolution ->
            val modes = if (resolution.ordinal % 2 == 0) {
                listOf(QueryMode.MATERIALIZED_VIEW, QueryMode.RAW)
            } else {
                listOf(QueryMode.RAW, QueryMode.MATERIALIZED_VIEW)
            }

            modes.associateWith { mode ->
                benchmarkQueryUseCase.mode = mode

                repeat(WARMUP_COUNT) {
                    request(resolution)
                }

                val samples = (1..MEASUREMENT_COUNT).map {
                    request(resolution)
                }

                val pointCount = samples.map { it.pointCount }.distinct()
                assertThat(pointCount).hasSize(1)

                Measurement(
                    p50Millis = percentileMillis(samples.map { it.elapsedNanos }, 0.50),
                    p95Millis = percentileMillis(samples.map { it.elapsedNanos }, 0.95),
                    minMillis = samples.minOf { it.elapsedNanos } / NANOS_PER_MILLISECOND,
                    maxMillis = samples.maxOf { it.elapsedNanos } / NANOS_PER_MILLISECOND,
                    pointCount = pointCount.single(),
                )
            }
        }

        printResults(results)

        results.forEach { (resolution, measurements) ->
            assertThat(measurements[QueryMode.MATERIALIZED_VIEW]!!.pointCount)
                .isEqualTo(measurements[QueryMode.RAW]!!.pointCount)

            val materializedViewP50 = measurements[QueryMode.MATERIALIZED_VIEW]!!.p50Millis
            val rawP50 = measurements[QueryMode.RAW]!!.p50Millis
            assertThat(materializedViewP50)
                .withFailMessage("MV p50 측정값이 0ms입니다: resolution=$resolution")
                .isGreaterThan(0.0)
            assertThat(rawP50)
                .withFailMessage("RAW p50 측정값이 0ms입니다: resolution=$resolution")
                .isGreaterThan(0.0)
        }
    }

    private fun request(resolution: AirQualityResolution): RequestMeasurement {
        val startedAt = System.nanoTime()
        val response = restClient.method(HttpMethod.GET)
            .uri(
                "/air-quality/timeseries" +
                    "?resolution=${resolution.code}" +
                    "&from=$TEST_FROM" +
                    "&to=$TEST_TO"
            )
            .accept(MediaType.APPLICATION_JSON)
            .exchange { _, clientResponse ->
                ResponseEntity
                    .status(clientResponse.statusCode)
                    .headers(clientResponse.headers)
                    .body(String(clientResponse.body.readAllBytes(), StandardCharsets.UTF_8))
            }
        val elapsedNanos = System.nanoTime() - startedAt

        assertThat(response.statusCode)
            .withFailMessage("응답 본문: %s", response.body)
            .isEqualTo(HttpStatus.OK)

        val body = mapper.readTree(response.body)
        assertThat(body.path("success").asBoolean() || body.path("isSuccess").asBoolean())
            .withFailMessage("응답 본문: %s", response.body)
            .isTrue

        val points = body.path("result").path("content")
        assertThat(points.isArray).isTrue

        return RequestMeasurement(
            elapsedNanos = elapsedNanos,
            pointCount = points.size(),
        )
    }

    private fun refreshMaterializedViews() {
        MATERIALIZED_VIEWS.forEach { viewName ->
            jdbcTemplate.execute("refresh materialized view $viewName")
        }
    }

    private fun deleteBenchmarkData() {
        jdbcTemplate.update(
            """
                delete from air_quality
                where sensor_measured_at >= ?
                  and sensor_measured_at < ?
                """.trimIndent(),
            TEST_FROM_TIMESTAMP,
            TEST_TO_TIMESTAMP,
        )
    }

    private fun percentileMillis(samples: List<Long>, percentile: Double): Double {
        val sorted = samples.sorted()
        val index = (percentile * (sorted.size - 1)).roundToInt()
        return sorted[index] / NANOS_PER_MILLISECOND
    }

    private fun printResults(
        results: Map<AirQualityResolution, Map<QueryMode, Measurement>>,
    ) {
        println()
        println("=== Air Quality Query Performance: 30 days x 1-second samples ===")
        println("resolution | mode | p50(ms) | p95(ms) | min(ms) | max(ms) | points")

        results.forEach { (resolution, measurements) ->
            QueryMode.entries.forEach { mode ->
                val measurement = measurements.getValue(mode)
                println(
                    String.format(
                        Locale.US,
                        "%-10s | %-4s | %7.2f | %7.2f | %7.2f | %7.2f | %d",
                        resolution.code,
                        mode.label,
                        measurement.p50Millis,
                        measurement.p95Millis,
                        measurement.minMillis,
                        measurement.maxMillis,
                        measurement.pointCount,
                    )
                )
            }

            val materializedViewP50 = measurements.getValue(QueryMode.MATERIALIZED_VIEW).p50Millis
            val rawP50 = measurements.getValue(QueryMode.RAW).p50Millis
            println(
                String.format(
                    Locale.US,
                    "speedup (%s): RAW / MV p50 = %.2fx",
                    resolution.code,
                    rawP50 / materializedViewP50,
                )
            )
        }
        println()
    }

    private data class RequestMeasurement(
        val elapsedNanos: Long,
        val pointCount: Int,
    )

    private data class Measurement(
        val p50Millis: Double,
        val p95Millis: Double,
        val minMillis: Double,
        val maxMillis: Double,
        val pointCount: Int,
    )

    @TestConfiguration(proxyBeanMethods = false)
    class TestConfig {
        @Bean("testObjectMapper")
        @Primary
        fun objectMapper(): ObjectMapper {
            return ObjectMapper().findAndRegisterModules()
        }

        @Bean
        @Primary
        fun benchmarkQueryUseCase(
            materializedViewService: AirQualityQueryService,
            jdbcTemplate: JdbcTemplate,
        ): BenchmarkQueryUseCase {
            return BenchmarkQueryUseCase(
                materializedViewService = materializedViewService,
                rawService = RawAirQualityQueryService(jdbcTemplate),
            )
        }
    }
}

enum class QueryMode(val label: String) {
    MATERIALIZED_VIEW("MV"),
    RAW("RAW"),
}

class BenchmarkQueryUseCase(
    private val materializedViewService: AirQualityQueryService,
    private val rawService: RawAirQualityQueryService,
) : QueryAirQualityUseCase {
    var mode: QueryMode = QueryMode.MATERIALIZED_VIEW

    override fun queryAirQuality(): GetAirQualityResponse {
        return materializedViewService.queryAirQuality()
    }

    override fun queryAirQualityTimeseries(
        dto: GetTimeseriesDTO,
    ): Slice<AirQualityTimeseriesPointDTO> {
        return when (mode) {
            QueryMode.MATERIALIZED_VIEW -> materializedViewService.queryAirQualityTimeseries(dto)
            QueryMode.RAW -> rawService.queryAirQualityTimeseries(dto)
        }
    }

    override fun queryAirQualityTimeseriesHistory(
        dto: GetTimeseriesHistoryDTO,
    ): Slice<AirQualityTimeseriesPointDTO> {
        return when (mode) {
            QueryMode.MATERIALIZED_VIEW -> materializedViewService.queryAirQualityTimeseriesHistory(dto)
            QueryMode.RAW -> materializedViewService.queryAirQualityTimeseriesHistory(dto)
        }
    }
}

class RawAirQualityQueryService(
    private val jdbcTemplate: JdbcTemplate,
) {
    fun queryAirQualityTimeseries(
        dto: GetTimeseriesDTO,
    ): Slice<AirQualityTimeseriesPointDTO> {
        val interval = intervalLiteral(dto.resolution)
        val sql = """
            select
                date_bin(
                    interval '$interval',
                    sensor_measured_at,
                    timestamp with time zone '1970-01-01 00:00:00+00'
                ) as bucket_start,
                date_bin(
                    interval '$interval',
                    sensor_measured_at,
                    timestamp with time zone '1970-01-01 00:00:00+00'
                ) + interval '$interval' as bucket_end,
                avg(pm25)::double precision as avg_pm25,
                max(pm25) as max_pm25,
                min(pm25) as min_pm25,
                avg(humidity) as avg_humidity,
                avg(temperature) as avg_temperature,
                avg(estimated_eco2ppm) as avg_eco2,
                avg(estimated_bvocppm) as avg_bvoc,
                count(*) as sample_count
            from air_quality
            where sensor_measured_at >= ?
              and sensor_measured_at < ?
            group by bucket_start
            order by bucket_start
            """.trimIndent()

        val points = jdbcTemplate.query(
            sql,
            PreparedStatementSetter { statement ->
                statement.setTimestamp(1, Timestamp.from(dto.from))
                statement.setTimestamp(2, Timestamp.from(dto.to))
            },
            RowMapper { resultSet, _ -> mapPoint(resultSet) },
        )

        return SliceImpl(points)
    }

    private fun mapPoint(
        resultSet: ResultSet,
    ): AirQualityTimeseriesPointDTO {
        val avgPm25 = resultSet.getDouble("avg_pm25")
        val avgEco2 = resultSet.getDouble("avg_eco2")
        val avgBvoc = resultSet.getDouble("avg_bvoc")

        return AirQualityTimeseriesPointDTO(
            time = instant(resultSet, "bucket_start").toZonedDateTime(),
            end = instant(resultSet, "bucket_end").toZonedDateTime(),
            pm25Quality = Quality.fromPm25(avgPm25),
            humidity = resultSet.getDouble("avg_humidity"),
            temperature = resultSet.getDouble("avg_temperature"),
            gasQuality = Quality.fromGas(
                eco2 = avgEco2,
                bvoc = avgBvoc,
            ),
            sampleCount = resultSet.getLong("sample_count"),
        )
    }

    private fun instant(resultSet: ResultSet, column: String): Instant {
        return resultSet.getObject(column, OffsetDateTime::class.java).toInstant()
    }

    private fun intervalLiteral(resolution: AirQualityResolution): String {
        return when (resolution) {
            AirQualityResolution.ONE_MINUTE -> "1 minute"
            AirQualityResolution.FIVE_MINUTES -> "5 minutes"
            AirQualityResolution.FIFTEEN_MINUTES -> "15 minutes"
            AirQualityResolution.ONE_HOUR -> "1 hour"
            AirQualityResolution.FOUR_HOURS -> "4 hours"
            AirQualityResolution.ONE_DAY -> "1 day"
        }
    }
}

private const val NANOS_PER_MILLISECOND = 1_000_000.0
