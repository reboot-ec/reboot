package com.reboot.organization.dto;

import com.reboot.organization.model.MembershipRole;
import com.reboot.organization.model.MembershipStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Organization membership details")
public record MembershipResponseDto(
        UUID id,
        UUID organizationId,
        UUID userId,
        MembershipRole role,
        MembershipStatus status,
        UUID invitedByUserId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
