package jnu.econovation.ecoknockbecentral.auth.controller

import jnu.econovation.ecoknockbecentral.EcoKnockBeCentralApplication
import jnu.econovation.ecoknockbecentral.auth.constant.AuthConstant.ACCESS_TOKEN
import jnu.econovation.ecoknockbecentral.airquality.service.AirQualityHistorySettingService
import jnu.econovation.ecoknockbecentral.common.security.util.JwtUtil
import jnu.econovation.ecoknockbecentral.member.dto.MemberInfoDTO
import jnu.econovation.ecoknockbecentral.member.model.entity.Member
import jnu.econovation.ecoknockbecentral.member.repository.MemberRepository
import jnu.econovation.ecoknockbecentral.overview.service.OverviewService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import org.springframework.web.client.RestClient
import java.time.Duration
import java.time.Instant

@SpringBootTest(
    classes = [EcoKnockBeCentralApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@ActiveProfiles("dev")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class GuestAuthorizationE2ETest(
    @param:LocalServerPort private val port: Int,
    private val memberRepository: MemberRepository,
    private val jwtUtil: JwtUtil,
    private val overviewService: OverviewService,
    private val airQualityHistorySettingService: AirQualityHistorySettingService,
    private val jdbcTemplate: JdbcTemplate,
) {
    private val memberIds = mutableListOf<Long>()
    private val restClient: RestClient = RestClient.builder()
        .baseUrl("http://localhost:$port")
        .build()

    @AfterEach
    fun tearDown() {
        memberIds.forEach {
            jdbcTemplate.update("delete from overview_shortcut where member_id = ?", it)
            jdbcTemplate.update("delete from overview_layout where member_id = ?", it)
            jdbcTemplate.update("delete from member_wallet where member_id = ?", it)
            jdbcTemplate.update("delete from member where id = ?", it)
        }
    }

    @Test
    @DisplayName("게스트는 자신의 overview를 조회·수정·초기화할 수 있지만 회원 전용 기능은 사용할 수 없다")
    fun guestCanManageOwnOverviewButNotRestrictedFeatures() {
        val accessToken = createGuestAccessToken()

        val shortcutUpdateStatus = requestPutStatus(
            "/overview/shortcuts",
            accessToken,
            """{"shortcuts":[{"iconUrl":null,"targetUrl":"https://guest.example.com","sortOrder":0,"name":"게스트 바로가기"}]}""",
        )
        val updatedShortcuts = requestBody("/overview/shortcuts", accessToken)
        val resetStatus = requestStatus("/overview/shortcuts/reset", accessToken, org.springframework.http.HttpMethod.PUT)
        val layoutUpdateStatus = requestPutStatus("/overview/layout", accessToken, """{"gridSize":2}""")
        val updatedLayout = requestBody("/overview/shortcuts", accessToken)
        val historySetting = requestBody("/air-quality/timeseries/history/default", accessToken)
        val historySettingUpdateStatus = requestPutStatus(
            "/air-quality/timeseries/history/default", accessToken, """{"resolution":"1h"}"""
        )
        val updatedHistorySetting = requestBody("/air-quality/timeseries/history/default", accessToken)

        val aiStatus = requestStatus("/ai/chat", accessToken, org.springframework.http.HttpMethod.POST)
        val aiHistoryStatus = requestStatus("/ai/chat/history", accessToken)
        val walletStatus = requestStatus("/wallet/me", accessToken)
        val adminStatus = requestStatus("/admin", accessToken)
        val unregisteredAdminStatus = requestStatus("/admin/not-registered", accessToken)

        assertThat(shortcutUpdateStatus).isEqualTo(HttpStatus.OK)
        assertThat(updatedShortcuts).contains("\"name\":\"게스트 바로가기\"")
        assertThat(resetStatus).isEqualTo(HttpStatus.OK)
        assertThat(layoutUpdateStatus).isEqualTo(HttpStatus.OK)
        assertThat(updatedLayout).contains("\"gridSize\":2")
        assertThat(historySetting).contains("\"resolution\":\"15m\"")
        assertThat(historySettingUpdateStatus).isEqualTo(HttpStatus.OK)
        assertThat(updatedHistorySetting).contains("\"resolution\":\"1h\"")
        assertThat(aiStatus).isEqualTo(HttpStatus.FORBIDDEN)
        assertThat(aiHistoryStatus).isEqualTo(HttpStatus.FORBIDDEN)
        assertThat(walletStatus).isEqualTo(HttpStatus.FORBIDDEN)
        assertThat(adminStatus).isEqualTo(HttpStatus.FORBIDDEN)
        assertThat(unregisteredAdminStatus).isEqualTo(HttpStatus.FORBIDDEN)
    }

    @Test
    @DisplayName("일반 회원과 관리자는 overview grid size를 수정할 수 있다")
    fun userAndAdminCanUpdateOverviewLayout() {
        val user = createMemberAccessToken(admin = false)
        val admin = createMemberAccessToken(admin = true)

        assertThat(requestPutStatus("/overview/layout", user, """{"gridSize":2}""")).isEqualTo(HttpStatus.OK)
        assertThat(requestPutStatus("/overview/layout", admin, """{"gridSize":2}""")).isEqualTo(HttpStatus.OK)
        assertThat(requestStatus("/ai/chat/history", user)).isEqualTo(HttpStatus.OK)
        assertThat(requestStatus("/ai/chat/history", admin)).isEqualTo(HttpStatus.OK)
        assertThat(requestStatus("/admin/not-registered", user)).isEqualTo(HttpStatus.FORBIDDEN)
    }

    private fun createGuestAccessToken(): String {
        val member = memberRepository.saveAndFlush(Member.createGuest(Instant.now().plus(Duration.ofHours(1))))
        memberIds += member.id
        overviewService.initializeOverview(member.id)
        airQualityHistorySettingService.initialize(member.id)

        return jwtUtil.generateAccessToken(MemberInfoDTO.from(member), Duration.ofHours(1))
    }

    private fun createMemberAccessToken(admin: Boolean): String {
        val member = Member.builder()
            .ssoMemberId(System.nanoTime())
            .cohort(jnu.econovation.ecoknockbecentral.member.model.vo.Cohort(1))
            .name("overview-layout-e2e")
            .status(jnu.econovation.ecoknockbecentral.member.model.vo.ActiveStatus.OB)
            .build()
        if (admin) {
            member.promoteToAdmin()
        }
        memberRepository.saveAndFlush(member)
        memberIds += member.id
        overviewService.initializeOverview(member.id)
        airQualityHistorySettingService.initialize(member.id)

        return jwtUtil.generateAccessToken(MemberInfoDTO.from(member), Duration.ofHours(1))
    }

    private fun requestPutStatus(path: String, accessToken: String, body: String): HttpStatus {
        return restClient.put()
            .uri(path)
            .header(HttpHeaders.COOKIE, "$ACCESS_TOKEN=$accessToken")
            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
            .body(body)
            .exchange { _, response -> HttpStatus.valueOf(response.statusCode.value()) }
    }

    private fun requestStatus(
        path: String,
        accessToken: String,
        method: org.springframework.http.HttpMethod = org.springframework.http.HttpMethod.GET,
    ): HttpStatus {
        return restClient.method(method)
            .uri(path)
            .header(HttpHeaders.COOKIE, "$ACCESS_TOKEN=$accessToken")
            .exchange { _, response -> HttpStatus.valueOf(response.statusCode.value()) }
    }

    private fun requestBody(path: String, accessToken: String): String {
        return restClient.get()
            .uri(path)
            .header(HttpHeaders.COOKIE, "$ACCESS_TOKEN=$accessToken")
            .exchange { _, response ->
                String(response.body.readAllBytes(), Charsets.UTF_8)
            }
    }
}
