package com.reboot.organization.service.impl;

import com.reboot.organization.dto.CreateMembershipRequestDto;
import com.reboot.organization.dto.MembershipResponseDto;
import com.reboot.organization.dto.PatchMembershipRequestDto;
import com.reboot.organization.exception.ApiException;
import com.reboot.organization.mapper.MembershipMapper;
import com.reboot.organization.model.MembershipRole;
import com.reboot.organization.model.MembershipStatus;
import com.reboot.organization.model.Organization;
import com.reboot.organization.model.OrganizationMembership;
import com.reboot.organization.repository.OrganizationMembershipRepository;
import com.reboot.organization.repository.OrganizationRepository;
import com.reboot.organization.service.MembershipService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class MembershipServiceImpl implements MembershipService {

    private static final Logger log = LoggerFactory.getLogger(MembershipServiceImpl.class);

    private final OrganizationRepository organizationRepository;
    private final OrganizationMembershipRepository membershipRepository;
    private final MembershipMapper membershipMapper;

    public MembershipServiceImpl(OrganizationRepository organizationRepository,
                                 OrganizationMembershipRepository membershipRepository,
                                 MembershipMapper membershipMapper) {
        this.organizationRepository = organizationRepository;
        this.membershipRepository = membershipRepository;
        this.membershipMapper = membershipMapper;
    }

    @Override
    @Transactional
    public MembershipResponseDto addMember(UUID organizationId,
                                           UUID authenticatedUserId,
                                           CreateMembershipRequestDto request) {
        Organization organization = lockOrganization(organizationId);
        OrganizationMembership actorMembership = requireOrganizationAdmin(organizationId, authenticatedUserId);
        if (request.role() == MembershipRole.OWNER && actorMembership.getRole() != MembershipRole.OWNER) {
            throw new AccessDeniedException("Only an organization owner may assign the OWNER role.");
        }

        OrganizationMembership membership = membershipRepository
                .findByOrganizationIdAndUserId(organizationId, request.userId())
                .orElseGet(OrganizationMembership::new);
        if (membership.getId() != null
                && (membership.getStatus() == MembershipStatus.ACTIVE
                || membership.getStatus() == MembershipStatus.PENDING)) {
            throw new ApiException(HttpStatus.CONFLICT, "MEMBERSHIP_ALREADY_EXISTS",
                    "The user already has an active or pending membership in this organization.");
        }

        membership.setOrganization(organization);
        membership.setUserId(request.userId());
        membership.setRole(request.role());
        membership.setStatus(MembershipStatus.PENDING);
        membership.setInvitedByUserId(authenticatedUserId);
        OrganizationMembership saved = membershipRepository.save(membership);
        log.info("Organization member invited: organizationId={}, memberId={}, actorUserId={}",
                organizationId, saved.getId(), authenticatedUserId);
        return membershipMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MembershipResponseDto> getMembers(UUID organizationId,
                                                  UUID authenticatedUserId,
                                                  boolean systemAdmin) {
        requireOrganization(organizationId);
        if (!systemAdmin && !hasActiveMembership(organizationId, authenticatedUserId)) {
            throw new AccessDeniedException("An active organization membership is required.");
        }
        return membershipRepository.findByOrganizationId(organizationId).stream()
                .map(membershipMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public MembershipResponseDto updateMember(UUID organizationId,
                                              UUID memberId,
                                              UUID authenticatedUserId,
                                              PatchMembershipRequestDto request) {
        lockOrganization(organizationId);
        OrganizationMembership actorMembership = requireOrganizationAdmin(organizationId, authenticatedUserId);
        OrganizationMembership target = membershipRepository.findByOrganizationIdAndId(organizationId, memberId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "MEMBERSHIP_NOT_FOUND",
                        "Organization membership was not found."));

        if (request.role() == null && request.status() == null) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "EMPTY_PATCH",
                    "At least one membership field must be provided.");
        }
        MembershipRole updatedRole = request.role() == null ? target.getRole() : request.role();
        MembershipStatus updatedStatus = request.status() == null ? target.getStatus() : request.status();
        boolean changesOwnerRole = target.getRole() == MembershipRole.OWNER
                || updatedRole == MembershipRole.OWNER;
        if (changesOwnerRole && actorMembership.getRole() != MembershipRole.OWNER) {
            throw new AccessDeniedException("Only an organization owner may assign or revoke the OWNER role.");
        }

        boolean removesActiveOwner = target.getRole() == MembershipRole.OWNER
                && target.getStatus() == MembershipStatus.ACTIVE
                && (updatedRole != MembershipRole.OWNER || updatedStatus != MembershipStatus.ACTIVE);
        if (removesActiveOwner
                && membershipRepository.countByOrganizationIdAndRoleAndStatus(
                organizationId, MembershipRole.OWNER, MembershipStatus.ACTIVE) <= 1) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "LAST_ACTIVE_OWNER",
                    "An organization must have at least one active owner.");
        }

        target.setRole(updatedRole);
        target.setStatus(updatedStatus);
        OrganizationMembership saved = membershipRepository.save(target);
        log.info("Organization membership updated: organizationId={}, memberId={}, actorUserId={}",
                organizationId, memberId, authenticatedUserId);
        return membershipMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void createOwnerMembership(UUID organizationId, UUID ownerUserId) {
        Organization organization = requireOrganization(organizationId);
        OrganizationMembership membership = new OrganizationMembership();
        membership.setOrganization(organization);
        membership.setUserId(ownerUserId);
        membership.setRole(MembershipRole.OWNER);
        membership.setStatus(MembershipStatus.ACTIVE);
        membership.setInvitedByUserId(ownerUserId);
        OrganizationMembership saved = membershipRepository.save(membership);
        log.info("Organization owner membership created: organizationId={}, memberId={}, actorUserId={}",
                organizationId, saved.getId(), ownerUserId);
    }

    private Organization lockOrganization(UUID organizationId) {
        return organizationRepository.findByIdForUpdate(organizationId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ORGANIZATION_NOT_FOUND",
                        "Organization was not found."));
    }

    private Organization requireOrganization(UUID organizationId) {
        return organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ORGANIZATION_NOT_FOUND",
                        "Organization was not found."));
    }

    private OrganizationMembership requireOrganizationAdmin(UUID organizationId, UUID userId) {
        OrganizationMembership membership = membershipRepository
                .findByOrganizationIdAndUserId(organizationId, userId)
                .filter(item -> item.getStatus() == MembershipStatus.ACTIVE)
                .filter(item -> item.getRole() == MembershipRole.OWNER || item.getRole() == MembershipRole.ADMIN)
                .orElseThrow(() -> new AccessDeniedException(
                        "An active organization owner or administrator is required."));
        return membership;
    }

    private boolean hasActiveMembership(UUID organizationId, UUID userId) {
        if (userId == null) {
            return false;
        }
        return membershipRepository.findByOrganizationIdAndUserId(organizationId, userId)
                .map(membership -> membership.getStatus() == MembershipStatus.ACTIVE)
                .orElse(false);
    }
}
