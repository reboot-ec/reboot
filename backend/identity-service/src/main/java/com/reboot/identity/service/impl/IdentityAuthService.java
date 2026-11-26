package com.reboot.identity.service.impl;

import com.reboot.identity.dto.AuthResponseDto;
import com.reboot.identity.dto.LoginRequestDto;
import com.reboot.identity.dto.RegisterRequestDto;
import com.reboot.identity.dto.UserResponseDto;
import com.reboot.identity.exception.ApiException;
import com.reboot.identity.mapper.IdentityUserMapper;
import com.reboot.identity.model.AuthProvider;
import com.reboot.identity.model.PasswordResetToken;
import com.reboot.identity.model.RefreshSession;
import com.reboot.identity.model.Role;
import com.reboot.identity.model.User;
import com.reboot.identity.model.UserStatus;
import com.reboot.identity.repository.PasswordResetTokenRepository;
import com.reboot.identity.repository.RefreshSessionRepository;
import com.reboot.identity.repository.RoleRepository;
import com.reboot.identity.repository.UserRepository;
import com.reboot.identity.service.DirectoryAuthenticator;
import com.reboot.identity.service.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class IdentityAuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshSessionRepository refreshSessionRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JwtService jwtService;
    private final DirectoryAuthenticator directoryAuthenticator;
    private final PasswordEncoder passwordEncoder;
    private final IdentityUserMapper userMapper;
    private final JavaMailSender mailSender;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${ad.enabled:false}")
    private boolean adEnabled;

    @Value("${jwt.access-expiration-ms:900000}")
    private long accessTokenExpirationMs;

    @Value("${jwt.refresh-expiration-ms:604800000}")
    private long refreshTokenExpirationMs;

    @Value("${app.password-reset.expiration-ms:1800000}")
    private long passwordResetExpirationMs;

    @Value("${app.password-reset.url:http://localhost:3000/reset-password}")
    private String passwordResetUrl;

    @Value("${app.mail.from:no-reply@reboot.local}")
    private String mailFrom;

    public IdentityAuthService(UserRepository userRepository,
                               RoleRepository roleRepository,
                               RefreshSessionRepository refreshSessionRepository,
                               PasswordResetTokenRepository passwordResetTokenRepository,
                               JwtService jwtService,
                               DirectoryAuthenticator directoryAuthenticator,
                               PasswordEncoder passwordEncoder,
                               IdentityUserMapper userMapper,
                               JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.refreshSessionRepository = refreshSessionRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.jwtService = jwtService;
        this.directoryAuthenticator = directoryAuthenticator;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.mailSender = mailSender;
    }

    @Transactional
    public UserResponseDto register(RegisterRequestDto request) {
        String email = request.getEmail().trim().toLowerCase();
        String username = request.getUsername().trim();
        if (userRepository.existsByEmail(email) || userRepository.existsByUsername(username)) {
            throw new ApiException(HttpStatus.CONFLICT, "IDENTITY_CONFLICT",
                    "An account with that email or username already exists.");
        }

        User user = userMapper.toNewUser(request);
        user.setEmail(email);
        user.setUsername(username);
        user.setFullName(request.getFullName().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setAuthProvider(AuthProvider.LOCAL);
        roleRepository.findByName("USER")
                .or(() -> roleRepository.findByName("ROLE_USER"))
                .ifPresent(role -> user.getRoles().add(role));

        User savedUser = userRepository.save(user);
        log.info("Registered local identity user: userId={}", savedUser.getId());
        return userMapper.toResponse(savedUser);
    }

    @Transactional
    public AuthResponseDto login(LoginRequestDto request) {
        String identifier = request.getUsernameOrEmail().trim();
        Optional<User> storedUser = userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByEmail(identifier.toLowerCase()));

        User user;
        if (storedUser.isPresent() && storedUser.get().getAuthProvider() == AuthProvider.LOCAL) {
            user = storedUser.get();
            if (user.getPasswordHash() == null
                    || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                rejectCredentials();
            }
        } else if (adEnabled) {
            try {
                user = directoryAuthenticator.authenticateAndSynchronize(identifier, request.getPassword());
            } catch (BadCredentialsException exception) {
                rejectCredentials();
                throw exception;
            }
        } else {
            rejectCredentials();
            throw new IllegalStateException("Unreachable authentication state.");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            rejectCredentials();
        }

        AuthResponseDto response = issueSession(user, request.getClientType(), request.getDeviceInfo());
        log.info("Identity login succeeded: userId={}, authProvider={}", user.getId(), user.getAuthProvider());
        return response;
    }

    @Transactional
    public AuthResponseDto refresh(String refreshToken) {
        String tokenHash = jwtService.hashToken(refreshToken);
        RefreshSession session = refreshSessionRepository.findByTokenHash(tokenHash)
                .orElseThrow(this::unauthorizedRefresh);
        OffsetDateTime now = OffsetDateTime.now();
        if (session.isRevoked() || !session.getExpiresAt().isAfter(now)) {
            throw unauthorizedRefresh();
        }

        User user = session.getUser();
        if (user.getStatus() != UserStatus.ACTIVE) {
            session.setRevoked(true);
            refreshSessionRepository.save(session);
            throw unauthorizedRefresh();
        }

        session.setRevoked(true);
        refreshSessionRepository.save(session);
        AuthResponseDto response = issueSession(user, session.getClientType(), session.getDeviceInfo());
        log.info("Refresh token rotated: userId={}", user.getId());
        return response;
    }

    @Transactional
    public void logout(UUID authenticatedUserId, String refreshToken) {
        String tokenHash = jwtService.hashToken(refreshToken);
        RefreshSession session = refreshSessionRepository.findByTokenHash(tokenHash)
                .orElseThrow(this::unauthorizedRefresh);
        if (!session.getUser().getId().equals(authenticatedUserId)) {
            log.warn("Logout rejected because refresh session belongs to another user: userId={}",
                    authenticatedUserId);
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN",
                    "The refresh session belongs to another user.");
        }
        session.setRevoked(true);
        refreshSessionRepository.save(session);
        log.info("Identity session revoked: userId={}", authenticatedUserId);
    }

    @Transactional
    public void requestPasswordReset(String email) {
        userRepository.findByEmail(email.trim().toLowerCase())
                .filter(user -> user.getAuthProvider() == AuthProvider.LOCAL)
                .ifPresent(this::createAndSendPasswordReset);
        log.info("Password reset request processed.");
    }

    @Transactional
    public void confirmPasswordReset(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(jwtService.hashToken(token))
                .orElseThrow(this::invalidPasswordResetToken);
        if (resetToken.isUsed() || !resetToken.getExpiresAt().isAfter(OffsetDateTime.now())) {
            throw invalidPasswordResetToken();
        }

        User user = resetToken.getUser();
        if (user.getAuthProvider() == AuthProvider.ACTIVE_DIRECTORY) {
            log.warn("Password reset rejected for an Active Directory identity: userId={}", user.getId());
            throw new ApiException(HttpStatus.FORBIDDEN, "DIRECTORY_PASSWORD_MANAGED_EXTERNALLY",
                    "Active Directory passwords must be changed through the directory service.");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
        revokeSessions(user);
        log.info("Local password reset completed: userId={}", user.getId());
    }

    private AuthResponseDto issueSession(User user, com.reboot.identity.model.ClientType clientType,
                                         String deviceInfo) {
        String refreshToken = jwtService.generateRefreshToken();
        RefreshSession session = new RefreshSession();
        session.setUser(user);
        session.setTokenHash(jwtService.hashToken(refreshToken));
        session.setClientType(clientType);
        session.setDeviceInfo(deviceInfo);
        session.setExpiresAt(OffsetDateTime.now().plus(Duration.ofMillis(refreshTokenExpirationMs)));
        refreshSessionRepository.save(session);

        AuthResponseDto response = new AuthResponseDto();
        response.setAccessToken(jwtService.generateAccessToken(user));
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(accessTokenExpirationMs / 1000);
        response.setUser(userMapper.toResponse(user));
        return response;
    }

    private void createAndSendPasswordReset(User user) {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        resetToken.setTokenHash(jwtService.hashToken(rawToken));
        resetToken.setExpiresAt(OffsetDateTime.now().plus(Duration.ofMillis(passwordResetExpirationMs)));
        passwordResetTokenRepository.save(resetToken);

        String resetLink = UriComponentsBuilder.fromUriString(passwordResetUrl)
                .queryParam("token", rawToken)
                .build()
                .toUriString();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(user.getEmail());
        message.setSubject("Reboot password reset");
        message.setText("Use this one-time link to reset your password: " + resetLink);
        mailSender.send(message);
    }

    private void revokeSessions(User user) {
        List<RefreshSession> sessions = refreshSessionRepository.findByUser(user);
        sessions.forEach(session -> session.setRevoked(true));
        refreshSessionRepository.saveAll(sessions);
    }

    private void rejectCredentials() {
        log.warn("Identity login rejected because credentials were invalid or account is inactive.");
        throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS",
                "The supplied credentials are invalid.");
    }

    private ApiException unauthorizedRefresh() {
        log.warn("Refresh or logout rejected because the refresh session is invalid.");
        return new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN",
                "The refresh token is invalid or expired.");
    }

    private ApiException invalidPasswordResetToken() {
        log.warn("Password reset rejected because the reset token is invalid or expired.");
        return new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_PASSWORD_RESET_TOKEN",
                "The password reset token is invalid or expired.");
    }
}
