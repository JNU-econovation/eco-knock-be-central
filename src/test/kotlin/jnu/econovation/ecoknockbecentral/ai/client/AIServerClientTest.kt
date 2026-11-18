package jnu.econovation.ecoknockbecentral.ai.client

import com.sun.net.httpserver.HttpServer
import jnu.econovation.ecoknockbecentral.ai.config.AIServerConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.client.RestClient
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import java.time.Duration

class AIServerClientTest {
    private lateinit var server: HttpServer
    private lateinit var contentType: String
    private lateinit var body: String

    @BeforeEach
    fun setUp() {
        server = HttpServer.create(InetSocketAddress(0), 0).apply {
            createContext("/chat") { exchange ->
                contentType = exchange.requestHeaders.getFirst("Content-Type")
                body = exchange.requestBody.readAllBytes().toString(StandardCharsets.UTF_8)

                val response = """{"answer":"답변","sources":[],"used_retrieval":false}"""
                    .toByteArray(StandardCharsets.UTF_8)
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
    fun `question 파트만 multipart form data로 AI 서버에 전송한다`() {
        val client = AIServerClient(
            AIServerConfig(
                baseUrl = "http://localhost:${server.address.port}",
                timeout = Duration.ofSeconds(3),
            ),
            RestClient.builder(),
        )

        client.chat("현재 질문")

        assertThat(contentType).startsWith("multipart/form-data")
        assertThat(body)
            .contains("name=\"question\"")
            .contains("현재 질문")
            .doesNotContain("name=\"file\"")
    }
}
