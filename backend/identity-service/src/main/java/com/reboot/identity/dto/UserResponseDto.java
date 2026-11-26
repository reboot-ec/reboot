package com.reboot.identity.dto;

import com.reboot.identity.model.AuthProvider;
import com.reboot.identity.model.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Public identity profile")
public record UserResponseDto(
        @Schema(example = "7ef7a2c2-d5d0-41ad-9c33-fde12a38521b") UUID id,
        @Schema(example = "user@reboot.local") String email,
        @Schema(example = "reboot-user") String username,
        @Schema(example = "Reboot User") String fullName,
        AuthProvider authProvider,
        UserStatus status,
        List<String> roles,
        OffsetDateTime createdAt) {
}
