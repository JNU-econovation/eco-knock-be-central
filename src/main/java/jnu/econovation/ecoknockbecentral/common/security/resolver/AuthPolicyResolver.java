package jnu.econovation.ecoknockbecentral.common.security.resolver;

import jakarta.servlet.http.HttpServletRequest;
import jnu.econovation.ecoknockbecentral.common.security.constant.AuthPolicy;
import jnu.econovation.ecoknockbecentral.common.security.dto.AuthRule;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AuthPolicyResolver {
    private final List<AuthRule> rules = List.of(
            AuthRule.skip("/auth/success"),
            AuthRule.skip("/actuator/health"),
            AuthRule.skip("/actuator/info"),
            AuthRule.skip("/actuator/prometheus"),
            AuthRule.skip("/air-quality/stream"),
            AuthRule.skip("/error"),
            AuthRule.skip("/favicon.ico"),
            AuthRule.skip("/oauth2/authorization/**"),
            AuthRule.skip("/login/**"),
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
