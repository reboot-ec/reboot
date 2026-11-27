package com.reboot.organization.dto;

import com.reboot.organization.model.OrganizationStatus;
import com.reboot.organization.model.OrganizationType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Organization details")
public record OrganizationResponseDto(
        UUID id,
        String name,
        String taxId,
        OrganizationType organizationType,
        String description,
        String contactEmail,
        String contactPhone,
        String address,
        OrganizationStatus status,
        UUID createdByUserId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
