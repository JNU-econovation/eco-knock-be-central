package jnu.econovation.ecoknockbecentral.common.security.filter;

import static jnu.econovation.ecoknockbecentral.common.constant.CommonConstant.criticalError;
import static jnu.econovation.ecoknockbecentral.common.security.constant.SecurityConstants.AUTHORIZATION_HEADER;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import jnu.econovation.ecoknockbecentral.common.security.handler.Rest401Handler;
import jnu.econovation.ecoknockbecentral.common.security.handler.Rest500Handler;
import jnu.econovation.ecoknockbecentral.common.security.helper.JwtAuthHelper;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Set<String> WHITELIST = Set.of(
            "/auth/success",
            "/error",
            "/favicon.ico",
            "/oauth2/authorization/**",
            "/login/**"
    );

    private static final Set<String> BLACKLIST = Set.of(

    );

    private static final Set<String> GREYLIST = Set.of(
    );

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final JwtAuthHelper helper;
    private final Rest401Handler rest401Handler;
    private final Rest500Handler rest500Handler;

    public JwtAuthFilter(
            JwtAuthHelper helper,
            Rest401Handler rest401Handler,
            Rest500Handler rest500Handler
    ) {
        this.helper = helper;
        this.rest401Handler = rest401Handler;
        this.rest500Handler = rest500Handler;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        boolean isWhitelisted = WHITELIST.stream()
                .anyMatch(pattern -> PATH_MATCHER.match(pattern, path));
        boolean isBlacklisted = BLACKLIST.stream()
                .anyMatch(pattern -> PATH_MATCHER.match(pattern, path));

        return isWhitelisted && !isBlacklisted;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        boolean isGreyList = GREYLIST.stream()
                .anyMatch(pattern -> PATH_MATCHER.match(pattern, request.getRequestURI()));
        boolean isBlackList = BLACKLIST.stream()
                .anyMatch(pattern -> PATH_MATCHER.match(pattern, request.getRequestURI()));
        boolean isGetMethod = HttpMethod.GET.matches(request.getMethod());

        try {
            Authentication auth = helper.authenticate(request.getHeader(AUTHORIZATION_HEADER));

            if (auth instanceof AbstractAuthenticationToken abstractAuthenticationToken) {
                abstractAuthenticationToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
            }

            SecurityContextHolder.getContext().setAuthentication(auth);
            filterChain.doFilter(request, response);

        } catch (Throwable exception) {
            if (isGreyList && isGetMethod && !isBlackList) {
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
            } else {
                handleException(exception, request, response);
            }
        }
    }

    private void handleException(
            Throwable e,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException, ServletException {
        SecurityContextHolder.clearContext();

        if (e instanceof AuthenticationException authenticationException) {
            rest401Handler.commence(request, response, authenticationException);
        } else {
            AuthenticationServiceException serviceException =
                    new AuthenticationServiceException(criticalError.apply("인증"), e);
            rest500Handler.commence(request, response, serviceException);
        }
    }
}