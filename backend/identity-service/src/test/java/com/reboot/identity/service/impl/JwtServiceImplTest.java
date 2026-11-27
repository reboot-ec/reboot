package com.reboot.identity.service.impl;

import com.reboot.identity.model.AuthProvider;
import com.reboot.identity.model.User;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceImplTest {

    private JwtServiceImpl jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtServiceImpl();
        ReflectionTestUtils.setField(jwtService, "jwtSecret",
                "unit-test-secret-that-is-long-enough-for-hmac-sha-256");
        ReflectionTestUtils.setField(jwtService, "accessTokenExpirationMs", 60_000L);
        jwtService.init();
    }

    @Test
    void signsAndValidatesAccessTokenWithUserSubject() {
        User user = new User();
        UUID userId = UUID.randomUUID();
        user.setId(userId);
        user.setEmail("unit@example.com");
        user.setAuthProvider(AuthProvider.LOCAL);

        String token = jwtService.generateAccessToken(user);

        assertTrue(jwtService.validateToken(token));
        Claims claims = jwtService.parseAllClaims(token);
        assertEquals(userId.toString(), claims.getSubject());
        assertEquals("unit@example.com", claims.get("email", String.class));
    }

    @Test
    void refreshTokensAreRandomAndOnlyTheirHashesAreStored() {
        String first = jwtService.generateRefreshToken();
        String second = jwtService.generateRefreshToken();

        assertNotEquals(first, second);
        assertNotEquals(first, jwtService.hashToken(first));
        assertEquals(jwtService.hashToken(first), jwtService.hashToken(first));
    }

    @Test
    void rejectsMalformedAccessToken() {
        assertFalse(jwtService.validateToken("not-a-jwt"));
    }
}
