package com.reboot.organization.service;

import com.reboot.organization.dto.CreateMembershipRequestDto;
import com.reboot.organization.dto.PatchMembershipRequestDto;
import com.reboot.organization.exception.ApiException;
import com.reboot.organization.mapper.MembershipMapper;
import com.reboot.organization.model.MembershipRole;
import com.reboot.organization.model.MembershipStatus;
import com.reboot.organization.model.Organization;
import com.reboot.organization.model.OrganizationMembership;
import com.reboot.organization.repository.OrganizationMembershipRepository;
import com.reboot.organization.repository.OrganizationRepository;
import com.reboot.organization.service.impl.MembershipServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MembershipServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private OrganizationMembershipRepository membershipRepository;

    @Mock
    private MembershipMapper membershipMapper;

    private MembershipServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new MembershipServiceImpl(
                organizationRepository, membershipRepository, membershipMapper);
    }

    @Test
    void rejectsUpdateThatWouldRemoveTheLastActiveOwner() {
        UUID organizationId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        Organization organization = new Organization();
        OrganizationMembership owner = membership(memberId, actorId,
                MembershipRole.OWNER, MembershipStatus.ACTIVE);

        when(organizationRepository.findByIdForUpdate(organizationId)).thenReturn(Optional.of(organization));
        when(membershipRepository.findByOrganizationIdAndUserId(organizationId, actorId))
                .thenReturn(Optional.of(owner));
        when(membershipRepository.findByOrganizationIdAndId(organizationId, memberId))
                .thenReturn(Optional.of(owner));
        when(membershipRepository.countByOrganizationIdAndRoleAndStatus(
                organizationId, MembershipRole.OWNER, MembershipStatus.ACTIVE)).thenReturn(1L);

        ApiException exception = assertThrows(ApiException.class, () -> service.updateMember(
                organizationId, memberId, actorId,
                new PatchMembershipRequestDto(null, MembershipStatus.REVOKED)));

        assertEquals(422, exception.getStatus().value());
        verify(membershipRepository, never()).save(owner);
    }

    @Test
    void rejectsDowngradingTheLastActiveOwner() {
        UUID organizationId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        Organization organization = new Organization();
        OrganizationMembership owner = membership(memberId, actorId,
                MembershipRole.OWNER, MembershipStatus.ACTIVE);

        when(organizationRepository.findByIdForUpdate(organizationId)).thenReturn(Optional.of(organization));
        when(membershipRepository.findByOrganizationIdAndUserId(organizationId, actorId))
                .thenReturn(Optional.of(owner));
        when(membershipRepository.findByOrganizationIdAndId(organizationId, memberId))
                .thenReturn(Optional.of(owner));
        when(membershipRepository.countByOrganizationIdAndRoleAndStatus(
                organizationId, MembershipRole.OWNER, MembershipStatus.ACTIVE)).thenReturn(1L);

        ApiException exception = assertThrows(ApiException.class, () -> service.updateMember(
                organizationId, memberId, actorId,
                new PatchMembershipRequestDto(MembershipRole.MEMBER, null)));

        assertEquals(422, exception.getStatus().value());
        verify(membershipRepository, never()).save(owner);
    }

    @Test
    void rejectsAdministratorChangingOwnerRole() {
        UUID organizationId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        Organization organization = new Organization();
        OrganizationMembership administrator = membership(UUID.randomUUID(), actorId,
                MembershipRole.ADMIN, MembershipStatus.ACTIVE);
        OrganizationMembership owner = membership(targetId, UUID.randomUUID(),
                MembershipRole.OWNER, MembershipStatus.ACTIVE);

        when(organizationRepository.findByIdForUpdate(organizationId)).thenReturn(Optional.of(organization));
        when(membershipRepository.findByOrganizationIdAndUserId(organizationId, actorId))
                .thenReturn(Optional.of(administrator));
        when(membershipRepository.findByOrganizationIdAndId(organizationId, targetId))
                .thenReturn(Optional.of(owner));

        assertThrows(AccessDeniedException.class, () -> service.updateMember(
                organizationId, targetId, actorId,
                new PatchMembershipRequestDto(MembershipRole.MEMBER, null)));
        verify(membershipRepository, never()).save(owner);
    }

    @Test
    void rejectsInvitingUserWithPendingMembership() {
        UUID organizationId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID inviteeId = UUID.randomUUID();
        Organization organization = new Organization();
        OrganizationMembership administrator = membership(UUID.randomUUID(), actorId,
                MembershipRole.ADMIN, MembershipStatus.ACTIVE);
        OrganizationMembership pending = membership(UUID.randomUUID(), inviteeId,
                MembershipRole.MEMBER, MembershipStatus.PENDING);

        when(organizationRepository.findByIdForUpdate(organizationId)).thenReturn(Optional.of(organization));
        when(membershipRepository.findByOrganizationIdAndUserId(organizationId, actorId))
                .thenReturn(Optional.of(administrator));
        when(membershipRepository.findByOrganizationIdAndUserId(organizationId, inviteeId))
                .thenReturn(Optional.of(pending));

        ApiException exception = assertThrows(ApiException.class, () -> service.addMember(
                organizationId, actorId, new CreateMembershipRequestDto(inviteeId, MembershipRole.MEMBER)));

        assertEquals(409, exception.getStatus().value());
        verify(membershipRepository, never()).save(pending);
    }

    @Test
    void rejectsMemberInvitingAnotherUser() {
        UUID organizationId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        Organization organization = new Organization();
        OrganizationMembership member = membership(UUID.randomUUID(), actorId,
                MembershipRole.MEMBER, MembershipStatus.ACTIVE);

        when(organizationRepository.findByIdForUpdate(organizationId)).thenReturn(Optional.of(organization));
        when(membershipRepository.findByOrganizationIdAndUserId(organizationId, actorId))
                .thenReturn(Optional.of(member));

        assertThrows(AccessDeniedException.class, () -> service.addMember(
                organizationId, actorId,
                new CreateMembershipRequestDto(UUID.randomUUID(), MembershipRole.MEMBER)));
        verify(membershipRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    private OrganizationMembership membership(UUID id,
                                              UUID userId,
                                              MembershipRole role,
                                              MembershipStatus status) {
        OrganizationMembership membership = new OrganizationMembership();
        membership.setId(id);
        membership.setUserId(userId);
        membership.setRole(role);
        membership.setStatus(status);
        return membership;
    }
}
