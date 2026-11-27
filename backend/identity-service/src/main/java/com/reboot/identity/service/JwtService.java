package com.reboot.identity.service;

import com.reboot.identity.model.User;
import io.jsonwebtoken.Claims;

import java.security.NoSuchAlgorithmException;
import java.util.Map;

public interface JwtService {

    String generateAccessToken(User user);

    String generateRefreshToken();

    String hashToken(String token);

    boolean validateToken(String token);

    Claims parseAllClaims(String token);

    String getUserIdFromToken(String token);
}
