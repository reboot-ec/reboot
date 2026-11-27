package com.reboot.organization.service.impl;

import com.reboot.organization.dto.CreateOrganizationRequestDto;
import com.reboot.organization.dto.OrganizationPageDto;
import com.reboot.organization.dto.OrganizationResponseDto;
import com.reboot.organization.dto.PatchOrganizationRequestDto;
import com.reboot.organization.exception.ApiException;
import com.reboot.organization.mapper.OrganizationMapper;
import com.reboot.organization.model.MembershipRole;
import com.reboot.organization.model.MembershipStatus;
import com.reboot.organization.model.Organization;
import com.reboot.organization.model.OrganizationType;
import com.reboot.organization.repository.OrganizationMembershipRepository;
import com.reboot.organization.repository.OrganizationRepository;
import com.reboot.organization.service.MembershipService;
import com.reboot.organization.service.OrganizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
public class OrganizationServiceImpl implements OrganizationService {

    private static final Logger log = LoggerFactory.getLogger(OrganizationServiceImpl.class);
    private static final int MAX_PAGE_SIZE = 100;

    private final OrganizationRepository organizationRepository;
    private final OrganizationMembershipRepository membershipRepository;
    private final OrganizationMapper organizationMapper;
    private final MembershipService membershipService;

    public OrganizationServiceImpl(OrganizationRepository organizationRepository,
                                   OrganizationMembershipRepository membershipRepository,
                                   OrganizationMapper organizationMapper,
                                   MembershipService membershipService) {
        this.organizationRepository = organizationRepository;
        this.membershipRepository = membershipRepository;
        this.organizationMapper = organizationMapper;
        this.membershipService = membershipService;
    }

    @Override
    @Transactional
    public OrganizationResponseDto create(UUID authenticatedUserId, CreateOrganizationRequestDto request) {
        Organization organization = organizationMapper.toEntity(request);
        organization.setCreatedByUserId(authenticatedUserId);
        Organization saved = organizationRepository.save(organization);
        membershipService.createOwnerMembership(saved.getId(), authenticatedUserId);
        log.info("Organization created: organizationId={}, actorUserId={}",
                saved.getId(), authenticatedUserId);
        return organizationMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationPageDto getOrganizations(int page, int pageSize, OrganizationType type, String name) {
        if (page < 0 || pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PAGINATION",
                    "Page must be non-negative and pageSize must be between 1 and 100.");
        }

        String nameFilter = StringUtils.hasText(name) ? name.trim() : null;
        Page<Organization> result = organizationRepository.search(
                type, nameFilter, PageRequest.of(page, pageSize, Sort.by(Sort.Direction.ASC, "name")));
        return new OrganizationPageDto(
                result.getContent().stream().map(organizationMapper::toDto).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationResponseDto getOrganization(UUID organizationId) {
        return organizationMapper.toDto(findOrganization(organizationId));
    }

    @Override
    @Transactional
    public OrganizationResponseDto update(UUID organizationId,
                                          UUID authenticatedUserId,
                                          boolean systemAdmin,
                                          PatchOrganizationRequestDto request) {
        Organization organization = findOrganization(organizationId);
        if (!systemAdmin && !hasOrganizationAdminRole(organizationId, authenticatedUserId)) {
            throw new AccessDeniedException("An active organization owner or administrator is required.");
        }
        if (isEmptyPatch(request)) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "EMPTY_PATCH",
                    "At least one organization field must be provided.");
        }

        organizationMapper.update(request, organization);
        Organization saved = organizationRepository.save(organization);
        log.info("Organization updated: organizationId={}, actorUserId={}",
                organizationId, authenticatedUserId);
        return organizationMapper.toDto(saved);
    }

    private Organization findOrganization(UUID organizationId) {
        return organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ORGANIZATION_NOT_FOUND",
                        "Organization was not found."));
    }

    private boolean hasOrganizationAdminRole(UUID organizationId, UUID userId) {
        if (userId == null) {
            return false;
        }
        return membershipRepository.findByOrganizationIdAndUserId(organizationId, userId)
                .map(membership -> membership.getStatus() == MembershipStatus.ACTIVE
                        && (membership.getRole() == MembershipRole.OWNER
                        || membership.getRole() == MembershipRole.ADMIN))
                .orElse(false);
    }

    private boolean isEmptyPatch(PatchOrganizationRequestDto request) {
        return request.name() == null
                && request.taxId() == null
                && request.organizationType() == null
                && request.description() == null
                && request.contactEmail() == null
                && request.contactPhone() == null
                && request.address() == null
                && request.status() == null;
    }
}
