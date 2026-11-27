package com.reboot.organization.service;

import com.reboot.organization.dto.CreateMembershipRequestDto;
import com.reboot.organization.dto.MembershipResponseDto;
import com.reboot.organization.dto.PatchMembershipRequestDto;

import java.util.List;
import java.util.UUID;

public interface MembershipService {

    MembershipResponseDto addMember(UUID organizationId,
                                    UUID authenticatedUserId,
                                    CreateMembershipRequestDto request);

    List<MembershipResponseDto> getMembers(UUID organizationId,
                                           UUID authenticatedUserId,
                                           boolean systemAdmin);

    MembershipResponseDto updateMember(UUID organizationId,
                                       UUID memberId,
                                       UUID authenticatedUserId,
                                       PatchMembershipRequestDto request);

    void createOwnerMembership(UUID organizationId, UUID ownerUserId);
}
