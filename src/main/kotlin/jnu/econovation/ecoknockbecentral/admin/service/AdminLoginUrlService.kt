package jnu.econovation.ecoknockbecentral.admin.service

import jnu.econovation.ecoknockbecentral.admin.constant.AdminConstant.ADMIN_HOME_PATH
import jnu.econovation.ecoknockbecentral.sso.resolver.SSORedirectUrlResolver
import jnu.econovation.ecoknockbecentral.common.security.config.UriSecurityConfig
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder

@Service
class AdminLoginUrlService(
    private val redirectUrlResolver: SSORedirectUrlResolver,
    private val uriSecurityConfig: UriSecurityConfig,
) {
    fun ssoLoginUrl(): String? {
        val redirectUrl = adminHomeUrl()

        return runCatching {
            redirectUrlResolver.resolveAdmin(redirectUrl)
            UriComponentsBuilder.fromPath("/sso/login")
                .queryParam("redirect", redirectUrl)
                .build()
                .toUriString()
        }.getOrNull()
    }

    private fun adminHomeUrl(): String {
        return "${uriSecurityConfig.adminOrigin().trimEnd('/')}$ADMIN_HOME_PATH"
    }
}
