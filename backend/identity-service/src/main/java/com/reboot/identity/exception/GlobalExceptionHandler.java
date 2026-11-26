package com.reboot.identity.exception;

import com.reboot.identity.dto.ApiErrorDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorDto> handleApiException(ApiException exception, HttpServletRequest request) {
        log.warn("Business request rejected: code={}, requestId={}", exception.getCode(), requestId());
        return error(exception.getStatus(), exception.getCode(), exception.getMessage(),
                exception.getDetails(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorDto> handleValidation(MethodArgumentNotValidException exception,
                                                        HttpServletRequest request) {
        List<String> details = exception.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .toList();
        log.warn("Request validation failed: requestId={}", requestId());
        return error(HttpStatus.UNPROCESSABLE_ENTITY, "VALIDATION_FAILED",
                "Request validation failed.", details, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorDto> handleConstraintViolation(ConstraintViolationException exception,
                                                                  HttpServletRequest request) {
        List<String> details = exception.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .toList();
        log.warn("Request constraint violation: requestId={}", requestId());
        return error(HttpStatus.UNPROCESSABLE_ENTITY, "VALIDATION_FAILED",
                "Request validation failed.", details, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorDto> handleUnreadableBody(HttpMessageNotReadableException exception,
                                                            HttpServletRequest request) {
        log.warn("Unreadable request body: requestId={}", requestId());
        return error(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST",
                "Request body is malformed.", List.of(), request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorDto> handleTypeMismatch(MethodArgumentTypeMismatchException exception,
                                                          HttpServletRequest request) {
        log.warn("Invalid request parameter: name={}, requestId={}", exception.getName(), requestId());
        return error(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER",
                "A request parameter has an invalid value.", List.of(exception.getName()), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorDto> handleConflict(DataIntegrityViolationException exception,
                                                      HttpServletRequest request) {
        log.warn("Persistence conflict: requestId={}", requestId());
        return error(HttpStatus.CONFLICT, "RESOURCE_CONFLICT",
                "The request conflicts with an existing resource.", List.of(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorDto> handleAccessDenied(AccessDeniedException exception,
                                                          HttpServletRequest request) {
        log.warn("Forbidden request: requestId={}", requestId());
        return error(HttpStatus.FORBIDDEN, "FORBIDDEN", "Access is forbidden.", List.of(), request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorDto> handleAuthentication(AuthenticationException exception,
                                                            HttpServletRequest request) {
        log.warn("Authentication rejected: requestId={}", requestId());
        return error(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED",
                "Authentication is required or invalid.", List.of(), request);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiErrorDto> handleNotFound(NoHandlerFoundException exception,
                                                      HttpServletRequest request) {
        log.warn("No route found: method={}, requestId={}", request.getMethod(), requestId());
        return error(HttpStatus.NOT_FOUND, "NOT_FOUND", "The requested resource was not found.",
                List.of(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorDto> handleUnexpected(Exception exception, HttpServletRequest request) {
        log.error("Unhandled request failure: requestId={}", requestId(), exception);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "An unexpected error occurred.", List.of(), request);
    }

    private ResponseEntity<ApiErrorDto> error(HttpStatus status, String code, String message,
                                              List<String> details, HttpServletRequest request) {
        String requestId = requestId();
        if (requestId == null) {
            requestId = UUID.randomUUID().toString();
        }
        return ResponseEntity.status(status)
                .body(new ApiErrorDto(new ApiErrorDto.ErrorBody(code, message, details, requestId)));
    }

    private String formatFieldError(FieldError fieldError) {
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }

    private String requestId() {
        return org.slf4j.MDC.get("requestId");
    }
}
