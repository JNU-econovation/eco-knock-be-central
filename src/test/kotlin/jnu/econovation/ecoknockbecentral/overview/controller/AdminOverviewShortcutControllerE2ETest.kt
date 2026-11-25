package jnu.econovation.ecoknockbecentral.overview.controller

import jnu.econovation.ecoknockbecentral.EcoKnockBeCentralApplication
import jnu.econovation.ecoknockbecentral.admin.config.AdminConfig
import jnu.econovation.ecoknockbecentral.admin.config.GrafanaConfig
import jnu.econovation.ecoknockbecentral.auth.constant.AuthConstant.ACCESS_TOKEN
import jnu.econovation.ecoknockbecentral.auth.constant.AuthConstant.REFRESH_TOKEN
import jnu.econovation.ecoknockbecentral.common.security.util.JwtUtil
import jnu.econovation.ecoknockbecentral.member.dto.MemberInfoDTO
import jnu.econovation.ecoknockbecentral.member.model.entity.Member
import jnu.econovation.ecoknockbecentral.member.model.vo.ActiveStatus
import jnu.econovation.ecoknockbecentral.member.model.vo.Cohort
import jnu.econovation.ecoknockbecentral.member.repository.MemberRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.*
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import org.springframework.web.client.RestClient
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@SpringBootTest(
    classes = [EcoKnockBeCentralApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("dev")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class AdminOverviewShortcutControllerE2ETest(
    @param:LocalServerPort
    private val port: Int,
    private val memberRepository: MemberRepository,
    private val jwtUtil: JwtUtil,
    private val jdbcTemplate: JdbcTemplate,
    private val adminConfig: AdminConfig,
    private val grafanaConfig: GrafanaConfig,
) {
    private val restClient: RestClient = RestClient.builder()
        .baseUrl("http://localhost:$port")
        .build()

    private var defaultShortcutBackup: List<DefaultShortcutBackup> = emptyList()

    @BeforeEach
    fun backUpDefaultShortcuts() {
        defaultShortcutBackup = jdbcTemplate.query(
            """
            select icon_url, target_url, sort_order, name
            from default_overview_shortcut
            order by sort_order
            """.trimIndent()
        ) { resultSet, _ ->
            DefaultShortcutBackup(
                iconUrl = resultSet.getString("icon_url"),
                targetUrl = resultSet.getString("target_url"),
                sortOrder = resultSet.getInt("sort_order"),
                name = resultSet.getString("name"),
            )
        }
    }

    @AfterEach
    fun tearDown() {
        jdbcTemplate.update(
            """
            delete from member
            where sso_member_id in (209902010001, 209902010002)
            """.trimIndent()
        )
        restoreDefaultShortcuts()
    }

    @Test
    @DisplayName("루트 경로는 인증 없이 관리자 화면으로 이동한다")
    fun rootRedirectsToAdminHome() {
        val response = request(HttpMethod.GET, "/")

        assertThat(response.statusCode).isEqualTo(HttpStatus.FOUND)
        assertThat(response.headers.location.toString()).endsWith("/admin")
    }

    @Test
    @DisplayName("미인증 관리자는 관리자 로그인 화면으로 이동한다")
    fun unauthenticatedAdminPageRedirectsToLogin() {
        val response = request(HttpMethod.GET, "/admin")

        assertThat(response.statusCode).isEqualTo(HttpStatus.FOUND)
        assertThat(response.headers.location.toString()).endsWith("/admin/login")
    }

    @Test
    @DisplayName("마스터 비밀번호가 맞으면 관리자 인증 쿠키를 발급하고 관리자 화면으로 이동한다")
    fun masterPasswordLoginIssuesAuthCookies() {
        val response = postForm(
            path = "/admin/login/master",
            body = adminMasterPasswordFormBody(),
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.FOUND)
        assertThat(response.headers.location.toString()).endsWith("/admin")
        assertThat(response.headers[HttpHeaders.SET_COOKIE])
            .anyMatch { it.startsWith("$ACCESS_TOKEN=") }
        assertThat(response.headers[HttpHeaders.SET_COOKIE])
            .anyMatch { it.startsWith("$REFRESH_TOKEN=") }
    }

    @Test
    @DisplayName("관리자 로그인 화면의 SSO 링크는 관리자 홈으로 이동하도록 생성된다")
    fun adminLoginPageUsesAdminHomeRedirect() {
        val response = request(HttpMethod.GET, "/admin/login")

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).contains("/sso/login")
        assertThat(response.body).contains("redirect=http://localhost:18081/admin")
        assertThat(response.body).doesNotContain("redirect=http://localhost:18081/admin/overview-shortcuts")
        assertThat(response.body).contains("/admin/login.css")
    }

    @Test
    @DisplayName("마스터 비밀번호가 틀리면 로그인 화면에 에러를 표시한다")
    fun wrongMasterPasswordReturnsLoginPageWithError() {
        val response = postForm(
            path = "/admin/login/master",
            body = "password=wrong-password",
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.headers[HttpHeaders.SET_COOKIE].orEmpty())
            .noneMatch { it.startsWith("$ACCESS_TOKEN=") || it.startsWith("$REFRESH_TOKEN=") }
        assertThat(response.body).contains("관리자 로그인 정보를 확인할 수 없습니다.")
    }

    @Test
    @DisplayName("마스터 비밀번호가 맞으면 관리자 계정의 일반 인증 쿠키를 발급한다")
    fun masterPasswordIssuesAdminAuthCookies() {
        val response = postJson(
            path = "/auth/admin",
            body = adminMasterPasswordJsonBody(),
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.NO_CONTENT)
        assertThat(response.headers[HttpHeaders.SET_COOKIE])
            .anyMatch { it.startsWith("$ACCESS_TOKEN=") }
        assertThat(response.headers[HttpHeaders.SET_COOKIE])
            .anyMatch { it.startsWith("$REFRESH_TOKEN=") }
    }

    @Test
    @DisplayName("관리자 인증 쿠키 발급은 마스터 비밀번호가 틀리면 실패한다")
    fun wrongMasterPasswordDoesNotIssueAdminAuthCookies() {
        val response = postJson(
            path = "/auth/admin",
            body = """{"password":"wrong-password"}""",
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        assertThat(response.headers[HttpHeaders.SET_COOKIE].orEmpty())
            .noneMatch { it.startsWith("$ACCESS_TOKEN=") || it.startsWith("$REFRESH_TOKEN=") }
    }

    @Test
    @DisplayName("관리자 마스터 로그인 API는 관리자 쿠키를 발급한다")
    fun adminMasterLoginApiIssuesAdminCookie() {
        val response = postJson(
            path = "/auth/admin",
            body = adminMasterPasswordJsonBody(),
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.NO_CONTENT)
        assertThat(response.headers[HttpHeaders.SET_COOKIE])
            .anyMatch { it.startsWith("$ACCESS_TOKEN=") }
        assertThat(response.headers[HttpHeaders.SET_COOKIE])
            .anyMatch { it.startsWith("$REFRESH_TOKEN=") }
    }

    @Test
    @DisplayName("관리자 마스터 로그인 API는 비밀번호가 틀리거나 누락되면 쿠키 없이 401 JSON 오류를 반환한다")
    fun invalidOrMissingAdminMasterPasswordReturnsUnauthorized() {
        listOf("""{"password":"wrong-password"}""", "{}").forEach { body ->
            val response = postJson(path = "/auth/admin", body = body)

            assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
            assertThat(response.body).contains("\"isSuccess\":false")
            assertThat(response.headers[HttpHeaders.SET_COOKIE].orEmpty())
                .noneMatch { it.startsWith("$ACCESS_TOKEN=") || it.startsWith("$REFRESH_TOKEN=") }
        }
    }

    @Test
    @DisplayName("관리자 로그인 API 쿠키로 관리자 JSON API에 접근할 수 있다")
    fun adminLoginApiCookieCanAccessAdminJsonApi() {
        val loginResponse = postJson(
            path = "/auth/admin",
            body = adminMasterPasswordJsonBody(),
        )
        val accessToken = loginResponse.headers[HttpHeaders.SET_COOKIE]
            .orEmpty()
            .first { it.startsWith("$ACCESS_TOKEN=") }
            .substringAfter("=")
            .substringBefore(";")

        val response = request(
            method = HttpMethod.GET,
            path = "/admin/api-docs-access",
            accessToken = accessToken,
        )

        assertThat(loginResponse.statusCode).isEqualTo(HttpStatus.NO_CONTENT)
        assertThat(jwtUtil.extractId(accessToken)).isZero
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    @DisplayName("관리자 access token으로 default overview shortcut 관리자 화면에 접근할 수 있다")
    fun accessTokenCanOpenOverviewShortcutPage() {
        val admin = saveMember(209902010002, promoteToAdmin = true)
        val response = request(
            method = HttpMethod.GET,
            path = "/admin/overview-shortcuts",
            accessToken = jwtUtil.generateAccessToken(MemberInfoDTO.from(admin)),
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.headers.contentType.toString()).startsWith(MediaType.TEXT_HTML_VALUE)
        assertThat(response.body).contains("기본 모아두기")
        assertThat(response.body).contains("previewRoot")
        assertThat(response.body).contains("previewShortcutGrid")
        assertThat(response.body).contains("/admin/overview-shortcuts.css")
        assertThat(response.body).contains("/admin/overview-shortcuts.js")
    }

    @Test
    @DisplayName("관리자 access token으로 관리자 홈에 접근할 수 있다")
    fun accessTokenCanOpenAdminHome() {
        val admin = saveMember(209902010002, promoteToAdmin = true)
        val response = request(
            method = HttpMethod.GET,
            path = "/admin/",
            accessToken = jwtUtil.generateAccessToken(MemberInfoDTO.from(admin)),
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.headers.contentType.toString()).startsWith(MediaType.TEXT_HTML_VALUE)
        assertThat(response.body).contains("관리자 기능")
        assertThat(response.body).contains("기본 모아두기 설정하기")
        assertThat(response.body).contains("/admin/overview-shortcuts")
        assertThat(response.body).contains("href=\"${grafanaConfig.url}\"")
        assertThat(response.body).contains("target=\"_blank\"")
        assertThat(response.body).contains("rel=\"noopener noreferrer\"")
    }

    @Test
    @DisplayName("관리자 화면 정적 CSS와 JS는 인증 없이 로드된다")
    fun adminStaticResourcesArePublic() {
        val cssResponse = request(HttpMethod.GET, "/admin/overview-shortcuts.css")
        val jsResponse = request(HttpMethod.GET, "/admin/overview-shortcuts.js")
        val adminCssResponse = request(HttpMethod.GET, "/admin/admin.css")
        val loginCssResponse = request(HttpMethod.GET, "/admin/login.css")
        val accessDeniedCssResponse = request(HttpMethod.GET, "/admin/access-denied.css")

        assertThat(cssResponse.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(cssResponse.body).contains("color-scheme: light")
        assertThat(cssResponse.body).contains("phone-preview")
        assertThat(jsResponse.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(jsResponse.body).contains("fetch(form.action")
        assertThat(jsResponse.body).contains("renderPreview")
        assertThat(adminCssResponse.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(adminCssResponse.body).contains("admin-home")
        assertThat(loginCssResponse.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(loginCssResponse.body).contains("primary-hover")
        assertThat(accessDeniedCssResponse.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(accessDeniedCssResponse.body).contains("place-items: center")
    }

    @Test
    @DisplayName("프로젝트 ADMIN은 default overview shortcut 관리자 화면에 접근할 수 있다")
    fun adminCanOpenOverviewShortcutPage() {
        val admin = saveMember(209902010001, promoteToAdmin = true)
        val response = request(
            method = HttpMethod.GET,
            path = "/admin/overview-shortcuts",
            accessToken = jwtUtil.generateAccessToken(MemberInfoDTO.from(admin)),
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.headers.contentType.toString()).startsWith(MediaType.TEXT_HTML_VALUE)
        assertThat(response.body).contains("기본 모아두기")
    }

    @Test
    @DisplayName("프로젝트 ADMIN은 관리자 홈에 접근할 수 있다")
    fun adminCanOpenAdminHome() {
        val admin = saveMember(209902010001, promoteToAdmin = true)
        val response = request(
            method = HttpMethod.GET,
            path = "/admin/",
            accessToken = jwtUtil.generateAccessToken(MemberInfoDTO.from(admin)),
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.headers.contentType.toString()).startsWith(MediaType.TEXT_HTML_VALUE)
        assertThat(response.body).contains("관리자 기능")
        assertThat(response.body).contains("기본 모아두기 설정하기")
        assertThat(response.body).contains("/admin/overview-shortcuts")
        assertThat(response.body).contains("href=\"${grafanaConfig.url}\"")
    }

    @Test
    @DisplayName("프로젝트 USER는 default overview shortcut 관리자 화면에 접근할 수 없다")
    fun userCannotOpenOverviewShortcutPage() {
        val user = saveMember(209902010002, promoteToAdmin = false)
        val response = request(
            method = HttpMethod.GET,
            path = "/admin/overview-shortcuts",
            accessToken = jwtUtil.generateAccessToken(MemberInfoDTO.from(user)),
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
        assertThat(response.body).contains("접근 권한이 없습니다")
        assertThat(response.body).contains("/admin/access-denied.css")
    }

    @Test
    @DisplayName("프로젝트 USER는 관리자 홈에 접근할 수 없다")
    fun userCannotOpenAdminHome() {
        val user = saveMember(209902010002, promoteToAdmin = false)
        val response = request(
            method = HttpMethod.GET,
            path = "/admin",
            accessToken = jwtUtil.generateAccessToken(MemberInfoDTO.from(user)),
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
        assertThat(response.body).contains("접근 권한이 없습니다")
        assertThat(response.body).contains("/admin/access-denied.css")
    }

    @Test
    @DisplayName("관리자는 JSON body로 default overview shortcut을 저장할 수 있다")
    fun adminCanSaveDefaultShortcutsWithJsonBody() {
        val admin = saveMember(209902010001, promoteToAdmin = true)
        val response = postJson(
            path = "/admin/overview-shortcuts",
            accessToken = jwtUtil.generateAccessToken(MemberInfoDTO.from(admin)),
            body = """
                {
                  "shortcuts": [
                    {
                      "sortOrder": 0,
                      "name": "첫째",
                      "iconUrl": "https://example.com/first-icon.png",
                      "targetUrl": "https://example.com/first"
                    },
                    {
                      "sortOrder": 1,
                      "name": "둘째",
                      "iconUrl": "https://example.com/second-icon.png",
                      "targetUrl": "https://example.com/second"
                    }
                  ]
                }
            """.trimIndent(),
        )

        val names = jdbcTemplate.queryForList(
            """
            select name
            from default_overview_shortcut
            order by sort_order
            """.trimIndent(),
            String::class.java
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).contains("\"isSuccess\":true")
        assertThat(names).containsExactly("첫째", "둘째")
    }

    @Test
    @DisplayName("관리자 저장 요청의 URL이 올바르지 않으면 JSON 에러를 반환한다")
    fun invalidAdminSaveReturnsJsonError() {
        val admin = saveMember(209902010001, promoteToAdmin = true)
        val response = postJson(
            path = "/admin/overview-shortcuts",
            accessToken = jwtUtil.generateAccessToken(MemberInfoDTO.from(admin)),
            body = """
                {
                  "shortcuts": [
                    {
                      "sortOrder": 0,
                      "name": "테스트",
                      "iconUrl": "ftp://example.com/icon.png",
                      "targetUrl": "https://example.com"
                    }
                  ]
                }
            """.trimIndent(),
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(response.body).contains("올바르지 않은 url 입니다.")
        assertThat(response.body).contains("\"isSuccess\":false")
    }

    @Test
    @DisplayName("관리자 로그아웃은 일반 인증 쿠키를 제거하고 로그인 화면으로 이동한다")
    fun logoutRemovesAuthCookies() {
        val admin = saveMember(209902010002, promoteToAdmin = true)
        val response = restClient.post()
            .uri("/admin/logout")
            .header(
                HttpHeaders.COOKIE,
                "$ACCESS_TOKEN=${jwtUtil.generateAccessToken(MemberInfoDTO.from(admin))}"
            )
            .exchange { _, response ->
                org.springframework.http.ResponseEntity
                    .status(response.statusCode)
                    .headers(response.headers)
                    .body(String(response.body.readAllBytes(), StandardCharsets.UTF_8))
            }

        assertThat(response.statusCode).isEqualTo(HttpStatus.FOUND)
        assertThat(response.headers.location.toString()).endsWith("/admin/login")
        assertThat(response.headers[HttpHeaders.SET_COOKIE])
            .anyMatch { it.startsWith("$ACCESS_TOKEN=;") }
        assertThat(response.headers[HttpHeaders.SET_COOKIE])
            .anyMatch { it.startsWith("$REFRESH_TOKEN=;") }
    }

    @Test
    @DisplayName("CSR 로그아웃은 현재 refresh 세션을 폐기하고 인증 쿠키를 삭제한다")
    fun csrLogoutRevokesRefreshSessionAndRemovesAuthCookies() {
        val loginResponse = postJson("/auth/admin", adminMasterPasswordJsonBody())
        val refreshToken = cookieValue(loginResponse, REFRESH_TOKEN)

        val logoutResponse = restClient.post()
            .uri("/auth/logout")
            .header(HttpHeaders.COOKIE, "$REFRESH_TOKEN=$refreshToken")
            .exchange { _, response ->
                ResponseEntity.status(response.statusCode).headers(response.headers).build<String>()
            }

        assertThat(logoutResponse.statusCode).isEqualTo(HttpStatus.NO_CONTENT)
        assertThat(logoutResponse.headers[HttpHeaders.SET_COOKIE])
            .anyMatch { it.startsWith("$ACCESS_TOKEN=;") }
        assertThat(logoutResponse.headers[HttpHeaders.SET_COOKIE])
            .anyMatch { it.startsWith("$REFRESH_TOKEN=;") }

        val reissueResponse = restClient.post()
            .uri("/auth/reissue")
            .header(HttpHeaders.COOKIE, "$REFRESH_TOKEN=$refreshToken")
            .exchange { _, response -> HttpStatus.valueOf(response.statusCode.value()) }

        assertThat(reissueResponse).isEqualTo(HttpStatus.UNAUTHORIZED)
    }

    @Test
    @DisplayName("오래된 refresh token으로 로그아웃해도 최신 refresh 세션은 유지한다")
    fun staleRefreshTokenDoesNotRevokeLatestSession() {
        val firstLogin = postJson("/auth/admin", adminMasterPasswordJsonBody())
        val staleRefreshToken = cookieValue(firstLogin, REFRESH_TOKEN)
        val latestLogin = postJson("/auth/admin", adminMasterPasswordJsonBody())
        val latestRefreshToken = cookieValue(latestLogin, REFRESH_TOKEN)

        val logoutStatus = restClient.post()
            .uri("/auth/logout")
            .header(HttpHeaders.COOKIE, "$REFRESH_TOKEN=$staleRefreshToken")
            .exchange { _, response -> HttpStatus.valueOf(response.statusCode.value()) }

        val reissueStatus = restClient.post()
            .uri("/auth/reissue")
            .header(HttpHeaders.COOKIE, "$REFRESH_TOKEN=$latestRefreshToken")
            .exchange { _, response -> HttpStatus.valueOf(response.statusCode.value()) }

        assertThat(logoutStatus).isEqualTo(HttpStatus.NO_CONTENT)
        assertThat(reissueStatus).isEqualTo(HttpStatus.OK)
    }

    private fun cookieValue(response: ResponseEntity<*>, name: String): String {
        return response.headers[HttpHeaders.SET_COOKIE]
            .orEmpty()
            .first { it.startsWith("$name=") }
            .substringAfter("=")
            .substringBefore(";")
    }

    private fun saveMember(
        ssoMemberId: Long,
        promoteToAdmin: Boolean,
    ): Member {
        val member = Member.builder()
            .ssoMemberId(ssoMemberId)
            .cohort(Cohort(1))
            .name("admin-overview-test-$ssoMemberId")
            .status(ActiveStatus.OB)
            .build()

        if (promoteToAdmin) {
            member.promoteToAdmin()
        }

        return memberRepository.save(member)
    }

    private fun request(
        method: HttpMethod,
        path: String,
        accessToken: String? = null,
    ): ResponseEntity<String> {
        return restClient.method(method)
            .uri(path)
            .headers {
                val cookies = listOfNotNull(
                    accessToken?.let { token -> "$ACCESS_TOKEN=$token" },
                )
                if (cookies.isNotEmpty()) {
                    it.add(HttpHeaders.COOKIE, cookies.joinToString("; "))
                }
            }
            .exchange { _, response ->
                ResponseEntity
                    .status(response.statusCode)
                    .headers(response.headers)
                    .body(String(response.body.readAllBytes(), StandardCharsets.UTF_8))
            }
    }

    private fun postForm(
        path: String,
        body: String,
    ): ResponseEntity<String> {
        return restClient.post()
            .uri(path)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(body)
            .exchange { _, response ->
                ResponseEntity
                    .status(response.statusCode)
                    .headers(response.headers)
                    .body(String(response.body.readAllBytes(), StandardCharsets.UTF_8))
            }
    }

    private fun adminMasterPasswordFormBody(): String {
        return "password=${URLEncoder.encode(adminConfig.masterPassword, StandardCharsets.UTF_8)}"
    }

    private fun adminMasterPasswordJsonBody(): String {
        return """{"password":"${escapeJson(adminConfig.masterPassword)}"}"""
    }

    private fun escapeJson(value: String): String {
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
    }

    private fun postJson(
        path: String,
        body: String,
    ): ResponseEntity<String> {
        return restClient.post()
            .uri(path)
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .exchange { _, response ->
                ResponseEntity
                    .status(response.statusCode)
                    .headers(response.headers)
                    .body(String(response.body.readAllBytes(), StandardCharsets.UTF_8))
            }
    }

    private fun postJson(
        path: String,
        accessToken: String,
        body: String,
    ): ResponseEntity<String> {
        return restClient.post()
            .uri(path)
            .header(HttpHeaders.COOKIE, "$ACCESS_TOKEN=$accessToken")
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .exchange { _, response ->
                org.springframework.http.ResponseEntity
                    .status(response.statusCode)
                    .headers(response.headers)
                    .body(String(response.body.readAllBytes(), StandardCharsets.UTF_8))
            }
    }

    private fun restoreDefaultShortcuts() {
        jdbcTemplate.update("delete from default_overview_shortcut")
        defaultShortcutBackup.forEach {
            jdbcTemplate.update(
                """
                insert into default_overview_shortcut (created_at, updated_at, icon_url, target_url, sort_order, name)
                values (now(), now(), ?, ?, ?, ?)
                """.trimIndent(),
                it.iconUrl,
                it.targetUrl,
                it.sortOrder,
                it.name,
            )
        }
    }

    private data class DefaultShortcutBackup(
        val iconUrl: String,
        val targetUrl: String,
        val sortOrder: Int,
        val name: String,
    )
}
