package com.reboot.identity.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomSecurityHandlers implements AuthenticationEntryPoint, AccessDeniedHandler {

    private static final Logger log = LoggerFactory.getLogger(CustomSecurityHandlers.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        String requestId = MDC.get("requestId");
        String uri = request.getRequestURI();
        log.warn("Unauthorized access attempt to path [{}] - requestId=[{}]: {}", uri, requestId, authException.getMessage());

        sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHORIZED", authException.getMessage(), requestId);
    }

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        String requestId = MDC.get("requestId");
        String uri = request.getRequestURI();
        log.warn("Access denied for path [{}] - requestId=[{}]: {}", uri, requestId, accessDeniedException.getMessage());

        sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", accessDeniedException.getMessage(), requestId);
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String code, String message, String requestId) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("code", code);
        errorDetails.put("message", message);
        errorDetails.put("details", Collections.emptyList());
        errorDetails.put("requestId", requestId != null ? requestId : "");

        Map<String, Object> body = new HashMap<>();
        body.put("error", errorDetails);

        objectMapper.writeValue(response.getWriter(), body);
    }
}
