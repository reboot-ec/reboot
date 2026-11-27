package com.reboot.identity.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Standard API error response")
public record ApiErrorDto(ErrorBody error) {

    @Schema(description = "Error payload")
    public record ErrorBody(
            @Schema(example = "VALIDATION_FAILED") String code,
            @Schema(example = "Request validation failed") String message,
            @Schema(description = "Validation or business error details") List<String> details,
            @Schema(description = "Request correlation identifier") String requestId) {
    }
}
