package com.reboot.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Standard successful API response")
public record ApiResponseDto<T>(
        @Schema(description = "Response payload") T data,
        @Schema(description = "Response metadata") ApiMetaDto meta) {
}
