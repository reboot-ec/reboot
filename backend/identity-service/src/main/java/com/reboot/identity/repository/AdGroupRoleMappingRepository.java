package com.reboot.identity.repository;

import com.reboot.identity.model.AdGroupRoleMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdGroupRoleMappingRepository extends JpaRepository<AdGroupRoleMapping, UUID> {

    Optional<AdGroupRoleMapping> findByAdGroupDn(String adGroupDn);

    boolean existsByAdGroupDn(String adGroupDn);
}
