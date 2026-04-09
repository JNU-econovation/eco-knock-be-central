package jnu.econovation.ecoknockbecentral.oauth2.handler

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jnu.econovation.ecoknockbecentral.common.cookie.constant.CookieConstant.REDIRECT_ORIGIN_KEY
import jnu.econovation.ecoknockbecentral.common.cookie.util.CookieUtil
import jnu.econovation.ecoknockbecentral.common.security.config.UriSecurityConfig
import jnu.econovation.ecoknockbecentral.common.security.constant.SecurityConstants.ACCESS_TOKEN
import jnu.econovation.ecoknockbecentral.common.security.dto.EcoKnockUserDetails
import jnu.econovation.ecoknockbecentral.common.security.util.JwtUtil
import mu.KotlinLogging
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import org.springframework.web.util.WebUtils

@Component
class OAuth2SuccessHandler(
    private val config: UriSecurityConfig,
    private val jwtUtil: JwtUtil
) : AuthenticationSuccessHandler {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val userDetails = authentication.principal as? EcoKnockUserDetails
            ?: run {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
                return
            }
        val accessToken: String = jwtUtil.generateToken(userDetails.memberInfo)

        val redirectOrigin: String = WebUtils
            .getCookie(request, REDIRECT_ORIGIN_KEY)
            ?.value
            ?: config.oAuth2.defaultRedirectOrigin

        CookieUtil.removeCookie(request, response, REDIRECT_ORIGIN_KEY)

        val redirectUrl = UriComponentsBuilder.fromUriString(redirectOrigin)
            .path(config.oAuth2.successPath)
            .queryParam(ACCESS_TOKEN, accessToken)
            .build()
            .toUriString()

        response.sendRedirect(redirectUrl)
    }

}