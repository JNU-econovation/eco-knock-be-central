package jnu.econovation.ecoknockbecentral.whozin.client

import jnu.econovation.ecoknockbecentral.common.exception.server.InternalServerException
import jnu.econovation.ecoknockbecentral.whozin.config.WhozinConfig
import jnu.econovation.ecoknockbecentral.whozin.dto.response.WhozinMembersResponse
import mu.KotlinLogging
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.body

@Component
class WhozinClient(
    val config: WhozinConfig,
    restClientBuilder: RestClient.Builder,
) {
    companion object {
        private const val MEMBERS_PATH = "open-api/v1/members"
        const val BEARER_PREFIX = "Bearer "
        private val logger = KotlinLogging.logger {}
    }

    private val client: RestClient = restClientBuilder
        .baseUrl(config.baseUrl)
        .build()

    fun getWhozinMembers(
        year: Int,
        month: Int,
        day: Int? = null
    ): WhozinMembersResponse {
        return try {
            client.get()
                .uri {
                    it.path(MEMBERS_PATH)
                        .queryParam("year", year)
                        .queryParam("month", month)
                        .apply { day?.let { queryParam("day", day) } }
                        .build()
                }
                .header(HttpHeaders.AUTHORIZATION, "${BEARER_PREFIX}${config.token}")
                .retrieve()
                .body<WhozinMembersResponse>()
                ?: throw InternalServerException(
                    IllegalStateException("Whozin 응답 본문이 비어 있음")
                )
        } catch (e: RestClientResponseException) {
            if (e.statusCode == HttpStatus.UNAUTHORIZED) {
                logger.warn { "Whozin 토큰이 만료 됐거나 올바르지 않은 토큰 : ${config.token.take(10)}..." }
            }

            throw InternalServerException(e)
        } catch (e: RestClientException) {
            throw InternalServerException(e)
        }
    }
}