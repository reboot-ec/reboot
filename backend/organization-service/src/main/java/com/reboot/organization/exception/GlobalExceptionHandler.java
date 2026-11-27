package com.reboot.organization.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApiException(ApiException exception,
                                                               HttpServletRequest request) {
        if (exception.getStatus() == HttpStatus.FORBIDDEN
                || exception.getStatus() == HttpStatus.CONFLICT
                || exception.getStatus() == HttpStatus.UNPROCESSABLE_ENTITY) {
            log.warn("Business request rejected: code={}, path={}, requestId={}",
                    exception.getCode(), request.getRequestURI(), requestId());
        }
        return error(exception.getStatus(), exception.getCode(), exception.getMessage(),
                exception.getDetails());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        List<String> details = exception.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .toList();
        log.warn("Request validation failed: requestId={}", requestId());
        return error(HttpStatus.UNPROCESSABLE_ENTITY, "VALIDATION_FAILED",
                "Request validation failed.", details);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException exception) {
        List<String> details = exception.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .toList();
        log.warn("Request constraint violation: requestId={}", requestId());
        return error(HttpStatus.UNPROCESSABLE_ENTITY, "VALIDATION_FAILED",
                "Request validation failed.", details);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableBody() {
        log.warn("Unreadable request body: requestId={}", requestId());
        return error(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST",
                "Request body is malformed.", List.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        log.warn("Invalid request parameter: name={}, requestId={}", exception.getName(), requestId());
        return error(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER",
                "A request parameter has an invalid value.", List.of(exception.getName()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict() {
        log.warn("Persistence conflict: requestId={}", requestId());
        return error(HttpStatus.CONFLICT, "RESOURCE_CONFLICT",
                "The request conflicts with an existing resource.", List.of());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied() {
        log.warn("Forbidden request: requestId={}", requestId());
        return error(HttpStatus.FORBIDDEN, "FORBIDDEN", "Access is forbidden.", List.of());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthentication() {
        log.warn("Authentication rejected: requestId={}", requestId());
        return error(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED",
                "Authentication is required or invalid.", List.of());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "NOT_FOUND",
                "The requested resource was not found.", List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception, HttpServletRequest request) {
        log.error("Unhandled request failure: path={}, requestId={}",
                request.getRequestURI(), requestId(), exception);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "An unexpected error occurred.", List.of());
    }

    private ResponseEntity<ApiErrorResponse> error(HttpStatus status,
                                                   String code,
                                                   String message,
                                                   List<String> details) {
        return ResponseEntity.status(status)
                .body(new ApiErrorResponse(new ApiErrorBody(
                        code, message, details, requestId())));
    }

    private String formatFieldError(FieldError fieldError) {
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }

    private String requestId() {
        String id = MDC.get("requestId");
        return id == null ? UUID.randomUUID().toString() : id;
    }

    public record ApiErrorResponse(ApiErrorBody error) {
    }

    public record ApiErrorBody(String code, String message, List<String> details, String requestId) {
    }
}
