package com.reboot.organization.repository;

import com.reboot.organization.model.OrganizationMembership;
import com.reboot.organization.model.MembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationMembershipRepository extends JpaRepository<OrganizationMembership, UUID> {

    List<OrganizationMembership> findByOrganizationId(UUID organizationId);

    List<OrganizationMembership> findByUserId(UUID userId);

    Optional<OrganizationMembership> findByOrganizationIdAndUserId(UUID organizationId, UUID userId);

    List<OrganizationMembership> findByOrganizationIdAndStatus(
        UUID organizationId,
        MembershipStatus status
    );
}
