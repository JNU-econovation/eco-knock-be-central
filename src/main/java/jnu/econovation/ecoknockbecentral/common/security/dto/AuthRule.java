package jnu.econovation.ecoknockbecentral.common.security.dto;

import jakarta.servlet.http.HttpServletRequest;
import jnu.econovation.ecoknockbecentral.common.security.constant.AuthPolicy;
import org.springframework.http.HttpMethod;
import org.springframework.util.AntPathMatcher;

import java.util.Set;

public record AuthRule(
        String pattern,
        Set<HttpMethod> methods,
        AuthPolicy policy
) {
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    public static AuthRule skip(String pattern) {
        return new AuthRule(pattern, null, AuthPolicy.SKIP);
    }

    public static AuthRule required(String pattern) {
        return new AuthRule(pattern, null, AuthPolicy.REQUIRED);
    }

    public static AuthRule optional(String pattern, HttpMethod... methods) {
        return new AuthRule(pattern, Set.of(methods), AuthPolicy.OPTIONAL);
    }

    public boolean matches(HttpServletRequest request) {
        boolean pathMatches = PATH_MATCHER.match(pattern, request.getRequestURI());
        boolean methodMatches = methods == null || methods.isEmpty()
                || methods.contains(HttpMethod.valueOf(request.getMethod()));

        return pathMatches && methodMatches;
    }
}