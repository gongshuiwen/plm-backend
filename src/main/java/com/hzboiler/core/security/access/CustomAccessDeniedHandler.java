package com.hzboiler.core.security.access;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hzboiler.core.protocal.R;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.hzboiler.core.exception.CoreBusinessExceptionEnums.ERROR_ACCESS_DENIED;

/**
 * @author gongshuiwen
 */
public record CustomAccessDeniedHandler(ObjectMapper objectMapper) implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(R.error(ERROR_ACCESS_DENIED)));
    }
}