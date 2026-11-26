package com.reboot.identity.service.impl;

import com.reboot.identity.dto.PasswordChangeRequestDto;
import com.reboot.identity.dto.ProfilePatchRequestDto;
import com.reboot.identity.dto.UserResponseDto;
import com.reboot.identity.exception.ApiException;
import com.reboot.identity.mapper.IdentityUserMapper;
import com.reboot.identity.model.AuthProvider;
import com.reboot.identity.model.RefreshSession;
import com.reboot.identity.model.User;
import com.reboot.identity.model.UserStatus;
import com.reboot.identity.repository.RefreshSessionRepository;
import com.reboot.identity.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class IdentityUserService {

    private final UserRepository userRepository;
    private final RefreshSessionRepository refreshSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final IdentityUserMapper userMapper;

    public IdentityUserService(UserRepository userRepository,
                               RefreshSessionRepository refreshSessionRepository,
                               PasswordEncoder passwordEncoder,
                               IdentityUserMapper userMapper) {
        this.userRepository = userRepository;
        this.refreshSessionRepository = refreshSessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @Transactional(readOnly = true)
    public UserResponseDto getMe(UUID authenticatedUserId) {
        return userMapper.toResponse(findUser(authenticatedUserId));
    }

    @Transactional
    public UserResponseDto updateMe(UUID authenticatedUserId, ProfilePatchRequestDto request) {
        if (request.getEmail() == null && request.getFullName() == null) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "EMPTY_PROFILE_UPDATE",
                    "At least one profile field must be provided.");
        }
        if (request.getEmail() != null && !StringUtils.hasText(request.getEmail())) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_EMAIL",
                    "Email must not be blank.");
        }
        if (request.getFullName() != null && !StringUtils.hasText(request.getFullName())) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_FULL_NAME",
                    "Full name must not be blank.");
        }

        User user = findUser(authenticatedUserId);
        if (StringUtils.hasText(request.getEmail())) {
            String email = request.getEmail().trim().toLowerCase();
            userRepository.findByEmail(email)
                    .filter(existing -> !existing.getId().equals(user.getId()))
                    .ifPresent(existing -> {
                        throw new ApiException(HttpStatus.CONFLICT, "EMAIL_IN_USE",
                                "That email address is already associated with another account.");
                    });
            user.setEmail(email);
        }
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName().trim());
        }
        userMapper.updateProfile(request, user);
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail().trim().toLowerCase());
        }
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName().trim());
        }
        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public void changePassword(UUID authenticatedUserId, PasswordChangeRequestDto request) {
        User user = findUser(authenticatedUserId);
        if (user.getAuthProvider() == AuthProvider.ACTIVE_DIRECTORY) {
            log.warn("Local password change blocked for Active Directory identity: userId={}", user.getId());
            throw new ApiException(HttpStatus.FORBIDDEN, "DIRECTORY_PASSWORD_MANAGED_EXTERNALLY",
                    "Active Directory passwords must be changed through the directory service.");
        }
        if (user.getPasswordHash() == null
                || !passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            log.warn("Password change rejected because current credentials were invalid: userId={}", user.getId());
            throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CURRENT_PASSWORD",
                    "The current password is invalid.");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "PASSWORD_UNCHANGED",
                    "The new password must differ from the current password.");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        revokeSessions(user);
        log.info("Local password changed: userId={}", user.getId());
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUser(UUID userId) {
        return userMapper.toResponse(findUser(userId));
    }

    @Transactional
    public UserResponseDto updateUserStatus(UUID adminUserId, UUID targetUserId, UserStatus status) {
        User target = findUser(targetUserId);
        target.setStatus(status);
        User saved = userRepository.save(target);
        if (status != UserStatus.ACTIVE) {
            revokeSessions(target);
        }
        log.info("Administrator changed user status: adminUserId={}, targetUserId={}, status={}",
                adminUserId, targetUserId, status);
        return userMapper.toResponse(saved);
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND",
                        "The requested user was not found."));
    }

    private void revokeSessions(User user) {
        List<RefreshSession> sessions = refreshSessionRepository.findByUser(user);
        sessions.forEach(session -> session.setRevoked(true));
        refreshSessionRepository.saveAll(sessions);
    }
}
