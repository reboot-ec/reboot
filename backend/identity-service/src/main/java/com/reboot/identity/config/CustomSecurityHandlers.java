package com.reboot.identity.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reboot.identity.dto.ApiErrorDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class CustomSecurityHandlers implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        String requestId = MDC.get("requestId");
        log.warn("Unauthorized access attempt: path={}, requestId={}", request.getRequestURI(), requestId);

        sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHORIZED",
                "Authentication is required or invalid.", requestId);
    }

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        String requestId = MDC.get("requestId");
        log.warn("Access denied: path={}, requestId={}", request.getRequestURI(), requestId);

        sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN",
                "Access is forbidden.", requestId);
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String code, String message, String requestId) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiErrorDto body = new ApiErrorDto(new ApiErrorDto.ErrorBody(
                code, message, List.of(), requestId != null ? requestId : ""));
        objectMapper.writeValue(response.getWriter(), body);
    }
}
