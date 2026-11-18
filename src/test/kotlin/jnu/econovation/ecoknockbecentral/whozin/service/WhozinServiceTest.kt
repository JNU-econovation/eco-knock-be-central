package jnu.econovation.ecoknockbecentral.whozin.service

import jnu.econovation.ecoknockbecentral.whozin.client.WhozinClient
import jnu.econovation.ecoknockbecentral.whozin.config.WhozinConfig
import mu.KotlinLogging
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.TestConstructor
import org.springframework.web.client.RestClient
import java.time.Duration
import java.time.LocalDate

@SpringBootTest(
    classes = [
        WhozinService::class,
        WhozinClient::class,
        WhozinServiceTest.TestConfig::class,
    ]
)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@DisabledIfEnvironmentVariable(
    named = "CI",
    matches = "true",
    disabledReason = "실제 Whozin API와 토큰이 필요하다. fork PR에서는 secret을 쓸 수 없어 CI에서는 건너뛴다.",
)
class WhozinServiceTest(
    private val whozinService: WhozinService,
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @Test
    @DisplayName("일 단위 조회는 실제 Whozin API 응답을 내부 DTO로 변환한다")
    fun getWhozinMembersByDay() {
        val result = whozinService.getWhozinMembers(
            year = 2026,
            month = 6,
            day = 26,
        )

        assertThat(result).isNotEmpty
        assertThat(result).allSatisfy { membersByDate ->
            assertThat(membersByDate.date).isEqualTo(LocalDate.of(2026, 6, 26))
            assertThat(membersByDate.users).isNotEmpty
            assertThat(membersByDate.users).allSatisfy { user ->
                assertThat(user.userId).isNotNull()
                assertThat(user.cohort.value()).isPositive()
                assertThat(user.name).isNotBlank()
                assertThat(user.presenceDuration).isGreaterThanOrEqualTo(Duration.ZERO)
            }
        }

        logger.info { "result = $result" }
    }

    @Test
    @DisplayName("월 단위 조회는 실제 Whozin API 응답을 날짜별 DTO 목록으로 반환한다")
    fun getWhozinMembersByMonth() {
        val result = whozinService.getWhozinMembers(
            year = 2026,
            month = 6,
        )

        assertThat(result).isNotEmpty
        assertThat(result).allSatisfy { membersByDate ->
            assertThat(membersByDate.date.year).isEqualTo(2026)
            assertThat(membersByDate.date.monthValue).isEqualTo(6)
            assertThat(membersByDate.users).allSatisfy { user ->
                assertThat(user.userId).isNotNull()
                assertThat(user.cohort.value()).isPositive()
                assertThat(user.name).isNotBlank()
                assertThat(user.presenceDuration).isGreaterThanOrEqualTo(Duration.ZERO)
            }
        }

        logger.info { "result = $result" }
    }

    @TestConfiguration
    @EnableConfigurationProperties(WhozinConfig::class)
    class TestConfig {
        @Bean
        fun restClientBuilder(): RestClient.Builder = RestClient.builder()
    }
}
