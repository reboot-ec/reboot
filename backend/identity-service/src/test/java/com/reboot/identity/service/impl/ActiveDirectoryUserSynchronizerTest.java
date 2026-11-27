package com.reboot.identity.service.impl;

import com.reboot.identity.model.AuthProvider;
import com.reboot.identity.model.Role;
import com.reboot.identity.model.User;
import com.reboot.identity.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ActiveDirectoryUserSynchronizerTest {

    @Test
    void synchronizesDirectoryUserWithoutLocalPasswordHash() {
        UserRepository userRepository = mock(UserRepository.class);
        AdGroupRoleMapper roleMapper = mock(AdGroupRoleMapper.class);
        Role rebootRole = new Role();
        rebootRole.setName("REBOOT");
        when(roleMapper.mapGroups(any())).thenReturn(Set.of(rebootRole));
        when(userRepository.findByUsername("jsoto")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("jsoto@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ActiveDirectoryUserSynchronizer synchronizer =
                new ActiveDirectoryUserSynchronizer(userRepository, roleMapper);

        User result = synchronizer.synchronize(
                "jsoto@example.com", "J Soto", "jsoto", new String[]{"CN=REBOOT"});

        assertEquals(AuthProvider.ACTIVE_DIRECTORY, result.getAuthProvider());
        assertNull(result.getPasswordHash());
        assertSame(rebootRole, result.getRoles().iterator().next());
    }
}
