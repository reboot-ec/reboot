package com.reboot.identity.service.impl;

import com.reboot.identity.model.AuthProvider;
import com.reboot.identity.model.Role;
import com.reboot.identity.model.User;
import com.reboot.identity.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class ActiveDirectoryUserSynchronizer {

    private final UserRepository userRepository;
    private final AdGroupRoleMapper adGroupRoleMapper;

    public ActiveDirectoryUserSynchronizer(UserRepository userRepository,
                                           AdGroupRoleMapper adGroupRoleMapper) {
        this.userRepository = userRepository;
        this.adGroupRoleMapper = adGroupRoleMapper;
    }

    @Transactional
    public User synchronize(String email, String displayName, String username, String[] memberOf) {
        if (!StringUtils.hasText(email) || !StringUtils.hasText(displayName) || !StringUtils.hasText(username)) {
            throw new IllegalStateException("Active Directory user is missing required profile attributes.");
        }

        Set<Role> assignedRoles = adGroupRoleMapper.mapGroups(memberOf);
        Optional<User> existingUser = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(email));
        User user = existingUser.orElseGet(User::new);
        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(displayName);
        user.setAuthProvider(AuthProvider.ACTIVE_DIRECTORY);
        user.setPasswordHash(null);
        user.setRoles(assignedRoles);

        User savedUser = userRepository.save(user);
        log.info("Synchronized Active Directory profile and group mappings: userId={}, roleCount={}",
                savedUser.getId(), assignedRoles.size());
        return savedUser;
    }
}
