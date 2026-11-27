package com.reboot.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response metadata")
public record ApiMetaDto(
        @Schema(description = "Request correlation identifier") String requestId) {
}
