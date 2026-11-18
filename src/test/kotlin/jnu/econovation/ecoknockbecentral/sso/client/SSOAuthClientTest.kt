package jnu.econovation.ecoknockbecentral.sso.client

import com.sun.net.httpserver.HttpServer
import jnu.econovation.ecoknockbecentral.sso.config.SSOConfig
import jnu.econovation.ecoknockbecentral.sso.exception.BadSSOTokenException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.client.RestClient
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets

class SSOAuthClientTest {
    private lateinit var server: HttpServer
    private lateinit var requestPath: String
    private lateinit var requestMethod: String
    private var requestAuthorization: String? = null
    private var requestCookie: String? = null
    private var responseStatus = 200

    @BeforeEach
    fun setUp() {
        server = HttpServer.create(InetSocketAddress(0), 0).apply {
            createContext("/api/eco-knock") { exchange ->
                requestPath = exchange.requestURI.path
                requestMethod = exchange.requestMethod
                requestAuthorization = exchange.requestHeaders.getFirst("Authorization")
                requestCookie = exchange.requestHeaders.getFirst("Cookie")

                if (responseStatus == 401) {
                    exchange.sendResponseHeaders(401, -1)
                    exchange.close()
                    return@createContext
                }

                val response = """{
                    "memberId": 1,
                    "name": "회원",
                    "generation": 16,
                    "status": "ACTIVE",
                    "roles": ["USER"]
                }""".trimIndent().toByteArray(StandardCharsets.UTF_8)
                exchange.responseHeaders.add("Content-Type", "application/json")
                exchange.sendResponseHeaders(200, response.size.toLong())
                exchange.responseBody.use { it.write(response) }
            }
            start()
        }
    }

    @AfterEach
    fun tearDown() {
        server.stop(0)
    }

    @Test
    fun `Gateway Passport 조회는 configured Gateway URL에 Bearer 토큰을 전송한다`() {
        val client = SSOAuthClient(
            SSOConfig(
                loginPageBaseUrl = "http://127.0.0.1:1",
                gatewayPassportUrl = "http://localhost:${server.address.port}/api/eco-knock",
                clientId = "client-id",
            ),
            RestClient.builder(),
        )

        val response = client.getPassport("sso-access-token")

        assertThat(response.memberId).isEqualTo(1L)
        assertThat(requestPath).isEqualTo("/api/eco-knock")
        assertThat(requestMethod).isEqualTo("POST")
        assertThat(requestAuthorization).isEqualTo("Bearer sso-access-token")
        assertThat(requestCookie).isNull()
    }

    @Test
    fun `Gateway가 401을 반환하면 유효하지 않은 SSO 토큰으로 처리한다`() {
        responseStatus = 401
        val client = SSOAuthClient(
            SSOConfig(
                loginPageBaseUrl = "http://127.0.0.1:1",
                gatewayPassportUrl = "http://localhost:${server.address.port}/api/eco-knock",
                clientId = "client-id",
            ),
            RestClient.builder(),
        )

        assertThatThrownBy { client.getPassport("invalid-token") }
            .isInstanceOf(BadSSOTokenException::class.java)
    }
}
