package com.reboot.organization.repository;

import com.reboot.organization.model.Organization;
import com.reboot.organization.model.OrganizationType;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {

    Optional<Organization> findByName(String name);

    Optional<Organization> findByTaxId(String taxId);

    boolean existsByName(String name);

    boolean existsByTaxId(String taxId);

    @Query("""
            select organization from Organization organization
            where (:type is null or organization.organizationType = :type)
              and (:name is null or lower(organization.name) like lower(concat('%', :name, '%')))
            """)
    Page<Organization> search(@Param("type") OrganizationType type,
                              @Param("name") String name,
                              Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select organization from Organization organization where organization.id = :id")
    Optional<Organization> findByIdForUpdate(@Param("id") UUID id);
}
