package com.reboot.organization.dto;

import com.reboot.organization.model.OrganizationStatus;
import com.reboot.organization.model.OrganizationType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Fields to update on an organization")
public record PatchOrganizationRequestDto(
        @NotBlank @Size(max = 255) String name,
        @Size(max = 100) String taxId,
        OrganizationType organizationType,
        String description,
        @Email @Size(max = 255) String contactEmail,
        @Size(max = 50) String contactPhone,
        String address,
        OrganizationStatus status) {
}
