package com.reboot.organization.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final SecretKey signingKey;

    public JwtAuthenticationFilter(@Value("${jwt.secret}") String jwtSecret) {
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractBearerToken(request);
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                Claims claims = Jwts.parser()
                        .verifyWith(signingKey)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                UUID userId = parseUserId(claims.getSubject());
                String email = claims.get("email", String.class);
                if (!StringUtils.hasText(email)) {
                    throw new IllegalArgumentException("JWT email claim is missing.");
                }

                List<SimpleGrantedAuthority> authorities = extractRoles(claims.get("roles"));
                JwtPrincipal principal = new JwtPrincipal(userId, email);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(principal, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JwtException | IllegalArgumentException exception) {
                SecurityContextHolder.clearContext();
                log.warn("JWT authentication rejected: requestId={}", org.slf4j.MDC.get("requestId"));
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractBearerToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (!StringUtils.hasText(authorization)
                || !authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return null;
        }
        String token = authorization.substring(7).trim();
        return token.isEmpty() ? null : token;
    }

    private UUID parseUserId(String subject) {
        if (!StringUtils.hasText(subject)) {
            throw new IllegalArgumentException("JWT subject is missing.");
        }
        return UUID.fromString(subject);
    }

    private List<SimpleGrantedAuthority> extractRoles(Object rolesClaim) {
        if (rolesClaim == null) {
            return List.of();
        }
        if (!(rolesClaim instanceof List<?> roles)) {
            throw new IllegalArgumentException("JWT roles claim must be a list.");
        }

        List<SimpleGrantedAuthority> authorities = new ArrayList<>(roles.size());
        for (Object roleClaim : roles) {
            if (!(roleClaim instanceof String role) || !StringUtils.hasText(role)) {
                throw new IllegalArgumentException("JWT roles claim contains an invalid role.");
            }
            String authority = role.toUpperCase(Locale.ROOT).startsWith("ROLE_")
                    ? role
                    : "ROLE_" + role;
            authorities.add(new SimpleGrantedAuthority(authority));
        }
        return authorities;
    }
}
