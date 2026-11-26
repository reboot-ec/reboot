package com.reboot.identity.service.impl;

import com.reboot.identity.model.AdGroupRoleMapping;
import com.reboot.identity.model.Role;
import com.reboot.identity.repository.AdGroupRoleMappingRepository;
import com.reboot.identity.repository.RoleRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AdGroupRoleMapperTest {

    private final AdGroupRoleMappingRepository mappingRepository =
            mock(AdGroupRoleMappingRepository.class);
    private final RoleRepository roleRepository = mock(RoleRepository.class);
    private final AdGroupRoleMapper mapper = new AdGroupRoleMapper(mappingRepository, roleRepository);

    @Test
    void mapsOnlyConfiguredDirectoryGroupDnsToRoles() {
        String rebootGroupDn = "CN=REBOOT,OU=Groups,DC=reboot,DC=com";
        Role rebootRole = new Role();
        rebootRole.setName("REBOOT");
        AdGroupRoleMapping mapping = new AdGroupRoleMapping();
        mapping.setAdGroupDn(rebootGroupDn);
        mapping.setRole(rebootRole);
        when(mappingRepository.findByAdGroupDn(rebootGroupDn)).thenReturn(Optional.of(mapping));
        when(mappingRepository.findByAdGroupDn("CN=Unmapped,OU=Groups,DC=reboot,DC=com"))
                .thenReturn(Optional.empty());

        Set<Role> roles = mapper.mapGroups(new String[]{
                rebootGroupDn, "CN=Unmapped,OU=Groups,DC=reboot,DC=com"
        });

        assertEquals(Set.of(rebootRole), roles);
        verifyNoInteractions(roleRepository);
    }

    @Test
    void fallsBackToUserRoleWhenNoDirectoryGroupIsMapped() {
        Role userRole = new Role();
        userRole.setName("USER");
        when(mappingRepository.findByAdGroupDn("CN=Unmapped")).thenReturn(Optional.empty());
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));

        assertEquals(Set.of(userRole), mapper.mapGroups(new String[]{"CN=Unmapped"}));
    }
}
