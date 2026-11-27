package com.reboot.organization.repository;

import com.reboot.organization.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {

    Optional<Organization> findByName(String name);

    Optional<Organization> findByTaxId(String taxId);

    boolean existsByName(String name);

    boolean existsByTaxId(String taxId);
}
