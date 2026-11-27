package com.reboot.organization.dto;

import com.reboot.organization.model.OrganizationType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Organization creation request")
public record CreateOrganizationRequestDto(
        @NotBlank @Size(max = 255) String name,
        @Size(max = 100) String taxId,
        @NotNull OrganizationType organizationType,
        String description,
        @Email @Size(max = 255) String contactEmail,
        @Size(max = 50) String contactPhone,
        String address) {
}
