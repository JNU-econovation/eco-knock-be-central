package jnu.econovation.ecoknockbecentral.sso.client

import jnu.econovation.ecoknockbecentral.common.exception.server.InternalServerException
import jnu.econovation.ecoknockbecentral.sso.config.SSOConfig
import jnu.econovation.ecoknockbecentral.sso.dto.response.SSOPassportResponse
import jnu.econovation.ecoknockbecentral.sso.exception.BadSSOTokenException
import mu.KotlinLogging
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.body

@Component
class SSOAuthClient(
    config: SSOConfig,
    restClientBuilder: RestClient.Builder,
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val client: RestClient = restClientBuilder.build()
    private val gatewayPassportUrl = config.gatewayPassportUrl

    fun getPassport(accessToken: String): SSOPassportResponse {
        return try {
            client.post()
                .uri(gatewayPassportUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
                .retrieve()
                .body<SSOPassportResponse>()
                ?: throw InternalServerException(
                    IllegalStateException("Gateway Passport 응답 본문이 비어 있음")
                )

        } catch (e: RestClientResponseException) {
            if (e.statusCode == HttpStatus.UNAUTHORIZED) {
                logger.warn(e) { "올바르지 않은 SSO 토큰" }
                throw BadSSOTokenException()
            }

            throw InternalServerException(e)
        } catch (e: RestClientException) {
            throw InternalServerException(e)
        }
    }
}
