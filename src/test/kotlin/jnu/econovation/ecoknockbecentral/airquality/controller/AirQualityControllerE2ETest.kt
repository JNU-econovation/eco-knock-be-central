package jnu.econovation.ecoknockbecentral.airquality.controller

import com.fasterxml.jackson.databind.ObjectMapper
import jnu.econovation.ecoknockbecentral.EcoKnockBeCentralApplication
import jnu.econovation.ecoknockbecentral.airquality.dto.request.AirQualityResolution
import jnu.econovation.ecoknockbecentral.airquality.dto.request.GetTimeseriesHistoryRequest
import jnu.econovation.ecoknockbecentral.airquality.dto.request.GetTimeseriesRequest
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

        insertAirQuality("2099-01-01T00:00:10Z", 10, 40.0, 20.0, 500.0, 0.10, 1)
        insertAirQuality("2099-01-01T00:02:10Z", 20, 60.0, 22.0, 700.0, 0.30, 2)
        insertAirQuality("2099-01-01T00:06:10Z", 30, 50.0, 24.0, 900.0, 0.50, 3)

        jdbcTemplate.execute("refresh materialized view air_quality_5m_mv")
    }

    @AfterEach
    fun tearDown() {
        deleteTestData()
        jdbcTemplate.execute("refresh materialized view air_quality_5m_mv")
    }

    @Test
    @DisplayName("타임시리즈 범위 조회는 토큰 없이 꺾은선 그래프 포인트를 반환한다")
    fun timeseriesReturnsLineGraphPointsWithoutToken() {
        val response = get(
            path = "/air-quality/timeseries",
            body = GetTimeseriesRequest(
                resolution = AirQualityResolution.FIVE_MINUTES,
                from = ZonedDateTime.parse("2099-01-01T00:00:00Z").toOffsetDateTime(),
                to = ZonedDateTime.parse("2099-01-01T00:10:00Z").toOffsetDateTime(),
            )
        )

        assertThat(response.statusCode)
            .withFailMessage("body: %s", response.body)
            .isEqualTo(HttpStatus.OK)

        val body = mapper.readTree(response.body)
        val points = body.path("content")

        assertThat(points).hasSize(2)
        assertThat(body.path("last").asBoolean()).isTrue()

        val first = points[0]
        assertThat(ZonedDateTime.parse(first.path("time").asText()).toInstant())
            .isEqualTo(Instant.parse("2099-01-01T00:00:00Z"))
        assertThat(first.path("pm25").asDouble()).isEqualTo(15.0)
        assertThat(first.path("pm25Min").asInt()).isEqualTo(10)
        assertThat(first.path("pm25Max").asInt()).isEqualTo(20)
        assertThat(first.path("sampleCount").asLong()).isEqualTo(2)

        val second = points[1]
        assertThat(ZonedDateTime.parse(second.path("time").asText()).toInstant())
            .isEqualTo(Instant.parse("2099-01-01T00:05:00Z"))
        assertThat(second.path("pm25").asDouble()).isEqualTo(30.0)
        assertThat(second.path("sampleCount").asLong()).isEqualTo(1)
    }

    @Test
    @DisplayName("타임시리즈 히스토리 조회는 before 이전 포인트를 최신순 limit 기준으로 잘라 반환한다")
    fun historyReturnsPreviousLineGraphPointsWithoutToken() {
        val response = get(
            path = "/air-quality/timeseries/history",
            body = GetTimeseriesHistoryRequest(
                resolution = AirQualityResolution.FIVE_MINUTES,
                before = ZonedDateTime.parse("2099-01-01T00:10:00Z").toOffsetDateTime(),
                limit = 1,
            )
        )

        assertThat(response.statusCode)
            .withFailMessage("body: %s", response.body)
            .isEqualTo(HttpStatus.OK)

        val body = mapper.readTree(response.body)
        val points = body.path("content")

        assertThat(points).hasSize(1)
        assertThat(body.path("last").asBoolean()).isFalse()
        assertThat(ZonedDateTime.parse(points[0].path("time").asText()).toInstant())
            .isEqualTo(Instant.parse("2099-01-01T00:05:00Z"))
        assertThat(points[0].path("pm25").asDouble()).isEqualTo(30.0)
    }

    @Test
    @DisplayName("지원하지 않는 resolution은 AirQuality 도메인 에러로 응답한다")
    fun timeseriesReturnsBusinessErrorForUnsupportedResolution() {
        val response = getRawJson(
            path = "/air-quality/timeseries",
            body = """
                {
                  "resolution": "10m",
                  "from": "2099-01-01T00:00:00Z",
                  "to": "2099-01-01T00:10:00Z"
                }
            """.trimIndent()
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)

        val body = mapper.readTree(response.body)
        assertThat(body.path("success").asBoolean()).isFalse()
        assertThat(body.path("errorCode").asText()).isEqualTo("AIR_QUALITY_400_002")
    }

    @Test
    @DisplayName("history limit이 범위를 벗어나면 AirQuality 도메인 에러로 응답한다")
    fun historyReturnsBusinessErrorForInvalidLimit() {
        val response = getRawJson(
            path = "/air-quality/timeseries/history",
            body = """
                {
                  "resolution": "5m",
                  "before": "2099-01-01T00:10:00Z",
                  "limit": 0
                }
            """.trimIndent()
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

    private fun get(
        path: String,
        body: Any,
    ): ResponseEntity<String> {
        return getRawJson(path, mapper.writeValueAsString(body))
    }

    private fun getRawJson(
        path: String,
        body: String,
    ): ResponseEntity<String> {
        return restClient.method(HttpMethod.GET)
            .uri(path)
            .body(body)
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
            delete from air_quality
            where sensor_measured_at >= ?
              and sensor_measured_at < ?
            """.trimIndent(),
            Timestamp.from(TEST_FROM),
            Timestamp.from(TEST_TO)
        )
    }

    private fun insertAirQuality(
        measuredAt: String,
        pm25: Int,
        humidity: Double,
        temperature: Double,
        eco2: Double,
        bvoc: Double,
        accuracy: Int,
    ) {
        val timestamp = Timestamp.from(Instant.parse(measuredAt))

        jdbcTemplate.update(
            """
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
            values (?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            timestamp,
            timestamp,
            pm25,
            humidity,
            temperature,
            eco2,
            bvoc,
            accuracy
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
