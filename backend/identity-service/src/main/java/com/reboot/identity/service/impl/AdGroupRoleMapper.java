package com.reboot.identity.service.impl;

import com.reboot.identity.model.AdGroupRoleMapping;
import com.reboot.identity.model.Role;
import com.reboot.identity.repository.AdGroupRoleMappingRepository;
import com.reboot.identity.repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class AdGroupRoleMapper {

    private final AdGroupRoleMappingRepository mappingRepository;
    private final RoleRepository roleRepository;

    public AdGroupRoleMapper(AdGroupRoleMappingRepository mappingRepository,
                             RoleRepository roleRepository) {
        this.mappingRepository = mappingRepository;
        this.roleRepository = roleRepository;
    }

    public Set<Role> mapGroups(String[] memberOf) {
        Set<Role> roles = new HashSet<>();
        if (memberOf != null) {
            for (String groupDn : memberOf) {
                mappingRepository.findByAdGroupDn(groupDn)
                        .map(AdGroupRoleMapping::getRole)
                        .ifPresent(roles::add);
            }
        }
        if (roles.isEmpty()) {
            Optional<Role> defaultRole = roleRepository.findByName("USER")
                    .or(() -> roleRepository.findByName("ROLE_USER"));
            defaultRole.ifPresent(roles::add);
        }
        return roles;
    }
}
