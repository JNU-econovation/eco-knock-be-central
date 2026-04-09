package jnu.econovation.ecoknockbecentral.common.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jnu.econovation.ecoknockbecentral.common.dto.response.CommonResponse;
import jnu.econovation.ecoknockbecentral.common.exception.constants.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static jnu.econovation.ecoknockbecentral.common.security.constant.SecurityConstants.AUTHORIZATION_HEADER;

@Component
@Slf4j
@RequiredArgsConstructor
public class Rest401Handler implements AuthenticationEntryPoint {
    private final ObjectMapper mapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        String shortenedHeader = authHeader != null
                ? authHeader.substring(0, Math.min(10, authHeader.length())) + "..."
                : null;

        log.warn(
                "인증 실패 -> {}, URI - {}, authorization header - {}",
                authException.getMessage(),
                request.getRequestURI(),
                shortenedHeader
        );

        mapper.writeValue(
                response.getWriter(),
                CommonResponse.ofFailure(ErrorCode.UNAUTHORIZED)
        );
    }
}