package jnu.econovation.ecoknockbecentral.common.security.handler;

import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jnu.econovation.ecoknockbecentral.common.dto.response.CommonResponse;
import jnu.econovation.ecoknockbecentral.common.exception.constants.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class Rest500Handler implements AuthenticationEntryPoint {
    private final ObjectMapper mapper;

    @Override
    public void commence(
            @NonNull HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        log.error(authException.getMessage(), authException.getCause());

        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        mapper.writeValue(
                response.getWriter(),
                CommonResponse.ofFailure(ErrorCode.INTERNAL_SERVER_ERROR)
        );
    }
}