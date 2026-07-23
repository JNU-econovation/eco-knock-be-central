package jnu.econovation.ecoknockbecentral.common.security.config;

import jakarta.servlet.http.HttpServletResponse;
import jnu.econovation.ecoknockbecentral.common.security.handler.Rest403Handler;
import jnu.econovation.ecoknockbecentral.common.security.filter.ApiDocAccessFilter;
import jnu.econovation.ecoknockbecentral.common.security.filter.JwtAuthFilter;
import jnu.econovation.ecoknockbecentral.common.security.filter.SSORedirectParamFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.ForwardedHeaderFilter;

import java.util.List;

import static jnu.econovation.ecoknockbecentral.common.security.constant.SecurityPath.ADMIN;
import static jnu.econovation.ecoknockbecentral.common.security.constant.SecurityPath.ADMIN_ACCESS_DENIED;
import static jnu.econovation.ecoknockbecentral.common.security.constant.SecurityPath.ADMIN_ALL;
import static jnu.econovation.ecoknockbecentral.common.security.constant.SecurityPath.ADMIN_LOGIN;
import static jnu.econovation.ecoknockbecentral.common.security.constant.SecurityPath.ADMIN_LOGIN_MASTER;
import static jnu.econovation.ecoknockbecentral.common.security.constant.SecurityPath.ADMIN_LOGOUT;
import static jnu.econovation.ecoknockbecentral.common.security.constant.SecurityPath.ADMIN_SLASH;
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

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private static final long CORS_MAX_AGE = 3600L;

    private final UriSecurityConfig uriSecurityConfig;
    private final ApiDocAccessFilter apiDocAccessFilter;
    private final JwtAuthFilter jwtAuthFilter;
    private final SSORedirectParamFilter ssoRedirectParamFilter;
    private final Rest403Handler rest403Handler;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(uriSecurityConfig.allowedFrontEndOrigins());
        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        );
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(CORS_MAX_AGE);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public FilterRegistrationBean<ForwardedHeaderFilter> forwardedHeaderFilter() {
        ForwardedHeaderFilter filter = new ForwardedHeaderFilter();
        FilterRegistrationBean<ForwardedHeaderFilter> registration =
                new FilterRegistrationBean<>(filter);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        return http
                .headers(headers -> headers.frameOptions(FrameOptionsConfig::sameOrigin))
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                AUTH_SUCCESS,
                                "/actuator/health",
                                "/actuator/info",
                                "/actuator/prometheus",
                                "/air-quality/stream",
                                SCALAR,
                                SCALAR_ALL,
                                OPENAPI_JSON,
                                OPENAPI_ALL,
                                OPENAPI_YAML,
                                "/error",
                                "/favicon.ico"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, ADMIN_LOGIN).permitAll()
                        .requestMatchers(HttpMethod.GET, ADMIN_STATIC_CSS, ADMIN_STATIC_JS).permitAll()
                        .requestMatchers(HttpMethod.POST, ADMIN_LOGIN_MASTER).permitAll()
                        .requestMatchers(HttpMethod.POST, ADMIN_LOGOUT).permitAll()
                        .requestMatchers(HttpMethod.GET, ADMIN_ACCESS_DENIED).permitAll()
                        .requestMatchers(ADMIN, ADMIN_SLASH, ADMIN_ALL).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, SSO_LOGIN).permitAll()
                        .requestMatchers(HttpMethod.GET, SSO_CALLBACK).permitAll()
                        .requestMatchers(HttpMethod.POST, SSO_PASSPORT).permitAll()
                        .requestMatchers(HttpMethod.POST, AUTH_REISSUE).permitAll()
                        .requestMatchers(HttpMethod.POST, AUTH_LOGOUT).permitAll()
                        .requestMatchers(HttpMethod.POST, AUTH_GUEST).permitAll()
                        .requestMatchers(HttpMethod.POST, AUTH_ADMIN).permitAll()
                        .requestMatchers(HttpMethod.GET, "/air-quality/timeseries/history/default")
                        .hasAnyRole("GUEST", "USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/air-quality/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/air-quality/timeseries/history/default")
                        .hasAnyRole("GUEST", "USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/profile").hasAnyRole("GUEST", "USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/overview/shortcuts").hasAnyRole("GUEST", "USER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/overview/shortcuts", "/overview/shortcuts/reset", "/overview/layout")
                        .hasAnyRole("GUEST", "USER", "ADMIN")
                        .anyRequest().hasAnyRole("USER", "ADMIN")
                )
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            String uri = request.getRequestURI();
                            if (ADMIN.equals(uri) || uri.startsWith(ADMIN_SLASH)) {
                                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                request.getRequestDispatcher(ADMIN_ACCESS_DENIED).forward(request, response);
                                return;
                            }

                            rest403Handler.handle(request, response, accessDeniedException);
                        })
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(
                        ssoRedirectParamFilter,
                        UsernamePasswordAuthenticationFilter.class
                )
                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class
                )
                .addFilterBefore(
                        apiDocAccessFilter,
                        JwtAuthFilter.class
                )
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .build();
    }
}
