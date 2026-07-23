package jnu.econovation.ecoknockbecentral.common.security.resolver;

import jakarta.servlet.http.HttpServletRequest;
import jnu.econovation.ecoknockbecentral.common.security.constant.AuthPolicy;
import jnu.econovation.ecoknockbecentral.common.security.dto.AuthRule;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.List;

import static jnu.econovation.ecoknockbecentral.common.security.constant.SecurityPath.ADMIN_ACCESS_DENIED;
import static jnu.econovation.ecoknockbecentral.common.security.constant.SecurityPath.ADMIN_LOGIN;
import static jnu.econovation.ecoknockbecentral.common.security.constant.SecurityPath.ADMIN_LOGIN_MASTER;
import static jnu.econovation.ecoknockbecentral.common.security.constant.SecurityPath.ADMIN_LOGOUT;
import static jnu.econovation.ecoknockbecentral.common.security.constant.SecurityPath.ADMIN_STATIC_CSS;
import static jnu.econovation.ecoknockbecentral.common.security.constant.SecurityPath.ADMIN_STATIC_JS;
import static jnu.econovation.ecoknockbecentral.common.security.constant.SecurityPath.AUTH_ADMIN;
import static jnu.econovation.ecoknockbecentral.common.security.constant.SecurityPath.AUTH_GUEST;
import static jnu.econovation.ecoknockbecentral.common.security.constant.SecurityPath.AUTH_LOGOUT;
import static jnu.econovation.ecoknockbecentral.common.security.constant.SecurityPath.AUTH_REISSUE;
import static jnu.econovation.ecoknockbecentral.common.security.constant.SecurityPath.AUTH_SUCCESS;
import static jnu.econovation.ecoknockbecentral.common.security.constant.SecurityPath.OPENAPI_ALL;
import static jnu.econovation.ecoknockbecentral.common.security.constant.SecurityPath.OPENAPI_JSON;
import static jnu.econovation.ecoknockbecentral.common.security.constant.SecurityPath.OPENAPI_YAML;
import static jnu.econovation.ecoknockbecentral.common.security.constant.SecurityPath.SCALAR;
import static jnu.econovation.ecoknockbecentral.common.security.constant.SecurityPath.SCALAR_ALL;
import static jnu.econovation.ecoknockbecentral.common.security.constant.SecurityPath.SSO_CALLBACK;
import static jnu.econovation.ecoknockbecentral.common.security.constant.SecurityPath.SSO_LOGIN;
import static jnu.econovation.ecoknockbecentral.common.security.constant.SecurityPath.SSO_PASSPORT;

@Component
public class AuthPolicyResolver {
    private final List<AuthRule> rules = List.of(
            AuthRule.skip(SSO_LOGIN, HttpMethod.GET),
            AuthRule.skip(SSO_CALLBACK, HttpMethod.GET),
            AuthRule.skip(SSO_PASSPORT, HttpMethod.POST),
            AuthRule.skip(AUTH_REISSUE, HttpMethod.POST),
            AuthRule.skip(AUTH_LOGOUT, HttpMethod.POST),
            AuthRule.skip(AUTH_GUEST, HttpMethod.POST),
            AuthRule.skip(AUTH_ADMIN, HttpMethod.POST),
            AuthRule.skip(AUTH_SUCCESS),
            AuthRule.skip(ADMIN_LOGIN, HttpMethod.GET),
            AuthRule.skip(ADMIN_STATIC_CSS, HttpMethod.GET),
            AuthRule.skip(ADMIN_STATIC_JS, HttpMethod.GET),
            AuthRule.skip(ADMIN_LOGIN_MASTER, HttpMethod.POST),
            AuthRule.skip(ADMIN_LOGOUT, HttpMethod.POST),
            AuthRule.skip(ADMIN_ACCESS_DENIED, HttpMethod.GET),
            AuthRule.skip("/actuator/health"),
            AuthRule.skip("/actuator/info"),
            AuthRule.skip("/actuator/prometheus"),
            AuthRule.skip("/air-quality/stream"),
            AuthRule.skip(SCALAR),
            AuthRule.skip(SCALAR_ALL),
            AuthRule.skip(OPENAPI_JSON),
            AuthRule.skip(OPENAPI_ALL),
            AuthRule.skip(OPENAPI_YAML),
            AuthRule.skip("/error"),
            AuthRule.skip("/favicon.ico"),
            AuthRule.required("/air-quality/timeseries/history/default"),
            AuthRule.optional("/air-quality/**", HttpMethod.GET),
            AuthRule.required("/**")
//          AuthRule.optional("/posts/**", HttpMethod.GET), optional 한 인증일 때 예시
    );

    public AuthPolicy resolve(HttpServletRequest request) {
        return rules.stream()
                .filter(rule -> rule.matches(request))
                .map(AuthRule::policy)
                .findFirst()
                .orElse(AuthPolicy.REQUIRED);
    }
}
