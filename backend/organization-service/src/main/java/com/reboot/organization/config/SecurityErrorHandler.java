package com.reboot.organization.config;

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
import java.util.List;
import java.util.UUID;

@Component
public class SecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private static final Logger log = LoggerFactory.getLogger(SecurityErrorHandler.class);
    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    private final ObjectMapper objectMapper;

    public SecurityErrorHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException exception) throws IOException {
        String requestId = requestId(response);
        log.warn("Unauthorized access attempt: path={}, requestId={}", request.getRequestURI(), requestId);
        writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHORIZED",
                "Authentication is required or invalid.", requestId);
    }

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException exception) throws IOException {
        String requestId = requestId(response);
        log.warn("Access denied: path={}, requestId={}", request.getRequestURI(), requestId);
        writeError(response, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN",
                "Access is forbidden.", requestId);
    }

    private String requestId(HttpServletResponse response) {
        String requestId = MDC.get("requestId");
        if (requestId == null) {
            requestId = UUID.randomUUID().toString();
            MDC.put("requestId", requestId);
            response.setHeader(REQUEST_ID_HEADER, requestId);
        }
        return requestId;
    }

    private void writeError(HttpServletResponse response,
                            int status,
                            String code,
                            String message,
                            String requestId) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(),
                new ErrorResponse(new ErrorBody(code, message, List.of(), requestId)));
    }

    public record ErrorResponse(ErrorBody error) {
    }

    public record ErrorBody(String code, String message, List<String> details, String requestId) {
    }
}
