package jnu.econovation.ecoknockbecentral.airquality.controller

import com.fasterxml.jackson.databind.ObjectMapper
import jnu.econovation.ecoknockbecentral.EcoKnockBeCentralApplication
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.client.RestClient
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.sql.Timestamp
import java.time.Instant
import java.time.ZonedDateTime

@SpringBootTest(
    classes = [EcoKnockBeCentralApplication::class, AirQualityControllerE2ETest.JacksonTestConfig::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@ExtendWith(SpringExtension::class)
class AirQualityControllerE2ETest(
    @param:LocalServerPort
    private val port: Int,
    private val jdbcTemplate: JdbcTemplate,
    private val mapper: ObjectMapper
) {
    private companion object {
        private val TEST_FROM = Instant.parse("2099-01-01T00:00:00Z")
        private val TEST_TO = Instant.parse("2099-01-01T00:10:00Z")
    }

    private val restClient: RestClient = RestClient.builder()
        .baseUrl("http://localhost:$port")
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build()

    @BeforeEach
    fun setUp() {
        deleteTestData()

        insertFiveMinuteAggregate(
            bucketStart = "2099-01-01T00:00:00Z",
            sumPm25 = 30L,
            maxPm25 = 20,
            minPm25 = 10,
            sumHumidity = 100.0,
            sumTemperature = 42.0,
            sumEco2 = 1200.0,
            sumBvoc = 0.40,
            sampleCount = 2L,
        )
        insertFiveMinuteAggregate(
            bucketStart = "2099-01-01T00:05:00Z",
            sumPm25 = 30L,
            maxPm25 = 30,
            minPm25 = 30,
            sumHumidity = 50.0,
            sumTemperature = 24.0,
            sumEco2 = 900.0,
            sumBvoc = 0.50,
            sampleCount = 1L,
        )
    }

    @AfterEach
    fun tearDown() {
        deleteTestData()
    }

    @Test
    @DisplayName("타임시리즈 범위 조회는 토큰 없이 꺾은선 그래프 포인트를 반환한다")
    fun timeseriesReturnsLineGraphPointsWithoutToken() {
        val response = get(
            "/air-quality/timeseries?resolution=5m&from=2099-01-01T00:00:00Z&to=2099-01-01T00:10:00Z"
        )

        assertThat(response.statusCode)
            .withFailMessage("body: %s", response.body)
            .isEqualTo(HttpStatus.OK)

        val body = mapper.readTree(response.body)
        val result = body.path("result")
        val points = result.path("content")

        assertThat(points).hasSize(2)
        assertThat(result.path("last").asBoolean()).isTrue()

        val first = points[0]
        assertThat(ZonedDateTime.parse(first.path("time").asText()).toInstant())
            .isEqualTo(Instant.parse("2099-01-01T00:00:00Z"))
        assertThat(first.path("pm25Quality").asText()).isEqualTo("GOOD")
        assertThat(first.path("gasQuality").asText()).isEqualTo("VERY_GOOD")
        assertThat(first.has("pm25")).isFalse()
        assertThat(first.has("pm25Min")).isFalse()
        assertThat(first.has("pm25Max")).isFalse()
        assertThat(first.has("eco2")).isFalse()
        assertThat(first.has("bvoc")).isFalse()
        assertThat(first.path("sampleCount").asLong()).isEqualTo(2)

        val second = points[1]
        assertThat(ZonedDateTime.parse(second.path("time").asText()).toInstant())
            .isEqualTo(Instant.parse("2099-01-01T00:05:00Z"))
        assertThat(second.path("pm25Quality").asText()).isEqualTo("NORMAL")
        assertThat(second.path("gasQuality").asText()).isEqualTo("GOOD")
        assertThat(second.path("sampleCount").asLong()).isEqualTo(1)
    }

    @Test
    @DisplayName("타임시리즈 히스토리 조회는 before 이전 포인트를 최신순 limit 기준으로 잘라 반환한다")
    fun historyReturnsPreviousLineGraphPointsWithoutToken() {
        val response = get(
            "/air-quality/timeseries/history?resolution=5m&before=2099-01-01T00:10:00Z&limit=1"
        )

        assertThat(response.statusCode)
            .withFailMessage("body: %s", response.body)
            .isEqualTo(HttpStatus.OK)

        val body = mapper.readTree(response.body)
        val result = body.path("result")
        val points = result.path("content")

        assertThat(points).hasSize(1)
        assertThat(result.path("last").asBoolean()).isFalse()
        assertThat(ZonedDateTime.parse(points[0].path("time").asText()).toInstant())
            .isEqualTo(Instant.parse("2099-01-01T00:05:00Z"))
        assertThat(points[0].path("pm25Quality").asText()).isEqualTo("NORMAL")
        assertThat(points[0].path("gasQuality").asText()).isEqualTo("GOOD")
    }

    @Test
    @DisplayName("지원하지 않는 resolution은 공통 입력 에러로 응답한다")
    fun timeseriesReturnsBusinessErrorForUnsupportedResolution() {
        val response = get(
            "/air-quality/timeseries?resolution=10m&from=2099-01-01T00:00:00Z&to=2099-01-01T00:10:00Z"
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)

        val body = mapper.readTree(response.body)
        assertThat(body.path("success").asBoolean()).isFalse()
        assertThat(body.path("errorCode").asText()).isEqualTo("COMMON_400_001")
    }

    @Test
    @DisplayName("history limit이 범위를 벗어나면 AirQuality 도메인 에러로 응답한다")
    fun historyReturnsBusinessErrorForInvalidLimit() {
        val response = get(
            "/air-quality/timeseries/history?resolution=5m&before=2099-01-01T00:10:00Z&limit=0"
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)

        val body = mapper.readTree(response.body)
        assertThat(body.path("success").asBoolean()).isFalse()
        assertThat(body.path("errorCode").asText()).isEqualTo("AIR_QUALITY_400_003")
    }

    @Test
    @DisplayName("SSE 스트림은 토큰 없이 connected 이벤트를 반환한다")
    fun streamReturnsSseConnectedEventWithoutToken() {
        val response = restClient.get()
            .uri("/air-quality/stream")
            .accept(MediaType.TEXT_EVENT_STREAM)
            .exchange { _, response ->
                val lines = BufferedReader(
                    InputStreamReader(response.body, StandardCharsets.UTF_8)
                ).use { reader ->
                    listOfNotNull(reader.readLine(), reader.readLine(), reader.readLine())
                }

                ResponseEntity
                    .status(response.statusCode)
                    .headers(response.headers)
                    .body(lines)
            }

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.headers.contentType.toString()).startsWith("text/event-stream")
        assertThat(response.body).anyMatch { it.contains("connected") }
        assertThat(response.body).anyMatch { it.contains("ok") }
    }

    private fun get(path: String): ResponseEntity<String> {
        return restClient.method(HttpMethod.GET)
            .uri(path)
            .exchange { _, response ->
                ResponseEntity
                    .status(response.statusCode)
                    .headers(response.headers)
                    .body(String(response.body.readAllBytes(), StandardCharsets.UTF_8))
            }
    }

    private fun deleteTestData() {
        jdbcTemplate.update(
            """
            delete from air_quality_5m_aggregate
            where bucket_start >= ?
              and bucket_start < ?
            """.trimIndent(),
            Timestamp.from(TEST_FROM),
            Timestamp.from(TEST_TO)
        )
    }

    private fun insertFiveMinuteAggregate(
        bucketStart: String,
        sumPm25: Long,
        maxPm25: Int,
        minPm25: Int,
        sumHumidity: Double,
        sumTemperature: Double,
        sumEco2: Double,
        sumBvoc: Double,
        sampleCount: Long,
    ) {
        val start = Instant.parse(bucketStart)

        jdbcTemplate.update(
            """
            insert into air_quality_5m_aggregate (
                bucket_start,
                bucket_end,
                sum_pm25,
                max_pm25,
                min_pm25,
                sum_humidity,
                sum_temperature,
                sum_eco2,
                sum_bvoc,
                sample_count
            )
            values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            Timestamp.from(start),
            Timestamp.from(start.plusSeconds(300)),
            sumPm25,
            maxPm25,
            minPm25,
            sumHumidity,
            sumTemperature,
            sumEco2,
            sumBvoc,
            sampleCount,
        )
    }

    @TestConfiguration
    class JacksonTestConfig {
        @Bean("testObjectMapper")
        @Primary
        fun objectMapper(): ObjectMapper {
            return ObjectMapper().findAndRegisterModules()
        }
    }
}
