package com.reboot.identity.service.impl;

import com.reboot.identity.dto.PasswordChangeRequestDto;
import com.reboot.identity.exception.ApiException;
import com.reboot.identity.mapper.IdentityUserMapper;
import com.reboot.identity.model.AuthProvider;
import com.reboot.identity.model.User;
import com.reboot.identity.repository.RefreshSessionRepository;
import com.reboot.identity.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class IdentityUserServiceTest {

    @Test
    void rejectsLocalPasswordChangeForDirectoryIdentity() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        User directoryUser = new User();
        UUID userId = UUID.randomUUID();
        directoryUser.setId(userId);
        directoryUser.setAuthProvider(AuthProvider.ACTIVE_DIRECTORY);
        when(userRepository.findById(userId)).thenReturn(Optional.of(directoryUser));

        IdentityUserService service = new IdentityUserService(
                userRepository, mock(RefreshSessionRepository.class),
                passwordEncoder, mock(IdentityUserMapper.class));
        PasswordChangeRequestDto request = new PasswordChangeRequestDto();
        request.setCurrentPassword("not-logged");
        request.setNewPassword("new-password-value");

        ApiException exception = assertThrows(ApiException.class,
                () -> service.changePassword(userId, request));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        verifyNoInteractions(passwordEncoder);
        verify(userRepository, never()).save(any(User.class));
    }
}
