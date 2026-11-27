package com.reboot.organization.service;

import com.reboot.organization.dto.CreateOrganizationRequestDto;
import com.reboot.organization.dto.OrganizationResponseDto;
import com.reboot.organization.dto.PatchOrganizationRequestDto;
import com.reboot.organization.mapper.MembershipMapper;
import com.reboot.organization.mapper.OrganizationMapper;
import com.reboot.organization.model.MembershipRole;
import com.reboot.organization.model.MembershipStatus;
import com.reboot.organization.model.Organization;
import com.reboot.organization.model.OrganizationMembership;
import com.reboot.organization.model.OrganizationType;
import com.reboot.organization.repository.OrganizationMembershipRepository;
import com.reboot.organization.repository.OrganizationRepository;
import com.reboot.organization.service.impl.MembershipServiceImpl;
import com.reboot.organization.service.impl.OrganizationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private OrganizationMembershipRepository membershipRepository;

    @Mock
    private OrganizationMapper organizationMapper;

    @Mock
    private MembershipMapper membershipMapper;

    private OrganizationService service;

    @BeforeEach
    void setUp() {
        MembershipService membershipService = new MembershipServiceImpl(
                organizationRepository, membershipRepository, membershipMapper);
        service = new OrganizationServiceImpl(
                organizationRepository, membershipRepository, organizationMapper, membershipService);
    }

    @Test
    void createsOrganizationAndPersistsCreatorAsActiveOwner() {
        UUID userId = UUID.randomUUID();
        UUID organizationId = UUID.randomUUID();
        CreateOrganizationRequestDto request = new CreateOrganizationRequestDto(
                "Repair Collective", null, OrganizationType.NGO, null, null, null, null);
        Organization organization = new Organization();
        OrganizationResponseDto response = new OrganizationResponseDto(
                organizationId, "Repair Collective", null, OrganizationType.NGO, null,
                null, null, null, null, userId, null, null);

        when(organizationMapper.toEntity(request)).thenReturn(organization);
        when(organizationRepository.save(organization)).thenAnswer(invocation -> {
            Organization saved = invocation.getArgument(0);
            saved.setId(organizationId);
            return saved;
        });
        when(organizationRepository.findById(organizationId)).thenReturn(Optional.of(organization));
        when(membershipRepository.save(any(OrganizationMembership.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(organizationMapper.toDto(organization)).thenReturn(response);

        assertEquals(response, service.create(userId, request));

        ArgumentCaptor<OrganizationMembership> membershipCaptor =
                ArgumentCaptor.forClass(OrganizationMembership.class);
        verify(membershipRepository).save(membershipCaptor.capture());
        OrganizationMembership membership = membershipCaptor.getValue();
        assertEquals(organizationId, membership.getOrganization().getId());
        assertEquals(userId, membership.getUserId());
        assertEquals(userId, membership.getInvitedByUserId());
        assertEquals(MembershipRole.OWNER, membership.getRole());
        assertEquals(MembershipStatus.ACTIVE, membership.getStatus());
        assertEquals(userId, organization.getCreatedByUserId());
    }

    @Test
    void rejectsMemberUpdatingOrganization() {
        UUID organizationId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        Organization organization = new Organization();
        OrganizationMembership member = new OrganizationMembership();
        member.setUserId(memberId);
        member.setRole(MembershipRole.MEMBER);
        member.setStatus(MembershipStatus.ACTIVE);
        when(organizationRepository.findById(organizationId)).thenReturn(Optional.of(organization));
        when(membershipRepository.findByOrganizationIdAndUserId(organizationId, memberId))
                .thenReturn(Optional.of(member));

        assertThrows(AccessDeniedException.class, () -> service.update(
                organizationId, memberId, false,
                new PatchOrganizationRequestDto("Changed name", null, null,
                        null, null, null, null, null)));
        verify(organizationMapper, never()).update(any(PatchOrganizationRequestDto.class), any());
    }
}
