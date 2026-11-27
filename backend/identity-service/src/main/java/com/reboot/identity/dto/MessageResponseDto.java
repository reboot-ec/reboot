package com.reboot.identity.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Operation result message")
public record MessageResponseDto(@Schema(example = "Operation completed") String message) {
}
