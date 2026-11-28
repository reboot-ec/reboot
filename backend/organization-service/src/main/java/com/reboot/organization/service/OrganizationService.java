package com.reboot.organization.service;

import com.reboot.organization.dto.CreateOrganizationRequestDto;
import com.reboot.organization.dto.OrganizationPageDto;
import com.reboot.organization.dto.OrganizationResponseDto;
import com.reboot.organization.dto.PatchOrganizationRequestDto;
import com.reboot.organization.model.OrganizationType;

import java.util.UUID;

public interface OrganizationService {

    OrganizationResponseDto create(UUID authenticatedUserId, CreateOrganizationRequestDto request);

    OrganizationPageDto getOrganizations(int page, int pageSize, OrganizationType type, String name);

    OrganizationPageDto getMyOrganizations(UUID userId, int page, int pageSize);

    OrganizationResponseDto getOrganization(UUID organizationId);

    OrganizationResponseDto update(UUID organizationId,
                                   UUID authenticatedUserId,
                                   boolean systemAdmin,
                                   PatchOrganizationRequestDto request);
}
