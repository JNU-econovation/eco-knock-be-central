package jnu.econovation.ecoknockbecentral.ai.client

import jnu.econovation.ecoknockbecentral.ai.config.AIServerConfig
import jnu.econovation.ecoknockbecentral.ai.dto.aiserver.response.AIServerResponse
import jnu.econovation.ecoknockbecentral.common.exception.server.InternalServerException
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.body
import java.time.Duration

@Component
class AIServerClient(
    config: AIServerConfig,
    restClientBuilder: RestClient.Builder,
) {
    companion object {
        private const val CHAT_PATH = "/chat"
    }

    private val client = restClientBuilder
        .baseUrl(config.baseUrl)
        .requestFactory(SimpleClientHttpRequestFactory().apply {
            setConnectTimeout(Duration.ofSeconds(3))
            setReadTimeout(config.timeout)
        })
        .build()

    fun chat(question: String): AIServerResponse {
        val body = MultipartBodyBuilder().apply {
            part("question", question)
        }.build()

        return try {
            client.post()
                .uri(CHAT_PATH)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body<AIServerResponse>()
                ?: throw InternalServerException(IllegalStateException("ai 서버의 응답이 null 임"))

        } catch (e: RestClientResponseException) {
            throw InternalServerException(e)
        }
    }
}
