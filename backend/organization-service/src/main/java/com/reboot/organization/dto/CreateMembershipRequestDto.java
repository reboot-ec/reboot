package com.reboot.organization.dto;

import com.reboot.organization.model.MembershipRole;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Invitation to join an organization")
public record CreateMembershipRequestDto(
        @NotNull UUID userId,
        @NotNull MembershipRole role) {
}
