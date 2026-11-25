package jnu.econovation.ecoknockbecentral.admin.controller

import io.swagger.v3.oas.annotations.Hidden
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jnu.econovation.ecoknockbecentral.admin.constant.AdminConstant.ADMIN_HOME_PATH
import jnu.econovation.ecoknockbecentral.admin.service.AdminLoginUrlService
import jnu.econovation.ecoknockbecentral.auth.config.AuthPolicyConfig
import jnu.econovation.ecoknockbecentral.auth.constant.AuthConstant.ACCESS_TOKEN
import jnu.econovation.ecoknockbecentral.auth.constant.AuthConstant.REFRESH_TOKEN
import jnu.econovation.ecoknockbecentral.auth.service.AuthService
import jnu.econovation.ecoknockbecentral.common.cookie.util.CookieUtil
import jnu.econovation.ecoknockbecentral.common.exception.client.ClientException
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/admin")
@Hidden
class AdminAuthController(
    private val authService: AuthService,
    private val authPolicyConfig: AuthPolicyConfig,
    private val adminLoginUrlService: AdminLoginUrlService,
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @GetMapping("/login")
    fun login(
        model: Model,
    ): String {
        model.addAttribute("ssoLoginUrl", adminLoginUrlService.ssoLoginUrl())
        return "admin/login"
    }

    @PostMapping("/login/master")
    fun loginWithMasterPassword(
        @RequestParam(name = "password", required = false) password: String?,
        request: HttpServletRequest,
        response: HttpServletResponse,
        model: Model,
    ): String {
        val tokens = try {
            authService.issueAdminToken(password)
        } catch (exception: ClientException) {
            logger.warn(exception) { "master login client exception" }
            model.addAttribute("ssoLoginUrl", adminLoginUrlService.ssoLoginUrl())
            model.addAttribute("errorMessage", "관리자 로그인 정보를 확인할 수 없습니다.")
            return "admin/login"
        }

        CookieUtil.addCookie(
            request,
            response,
            ACCESS_TOKEN,
            tokens.accessToken,
            authPolicyConfig.accessTokenTTL.toSeconds().toInt(),
        )
        CookieUtil.addCookie(
            request,
            response,
            REFRESH_TOKEN,
            tokens.refreshToken,
            authPolicyConfig.refreshTokenTTL.toSeconds().toInt(),
        )

        return "redirect:$ADMIN_HOME_PATH"
    }

    @PostMapping("/logout")
    fun logout(
        @CookieValue(name = REFRESH_TOKEN, required = false) refreshToken: String?,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): String {
        authService.logout(refreshToken)
        CookieUtil.removeCookie(request, response, ACCESS_TOKEN)
        CookieUtil.removeCookie(request, response, REFRESH_TOKEN)
        return "redirect:/admin/login"
    }

    @GetMapping("/access-denied")
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun accessDenied(): String {
        return "admin/access-denied"
    }
}
