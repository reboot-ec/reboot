package com.reboot.identity.config;

import com.reboot.identity.model.User;
import com.reboot.identity.repository.UserRepository;
import com.reboot.identity.service.JwtService;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);
            if (jwt != null && jwtService.validateToken(jwt)) {
                Claims claims = jwtService.parseAllClaims(jwt);
                String userIdStr = claims.getSubject();

                if (userIdStr != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UUID userId = UUID.fromString(userIdStr);
                    User user = userRepository.findById(userId).orElse(null);

                    if (user != null && user.getStatus() == com.reboot.identity.model.UserStatus.ACTIVE) {
                        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();

                        @SuppressWarnings("unchecked")
                        List<String> roles = claims.get("roles", List.class);
                        if (roles != null) {
                            for (String role : roles) {
                                String authority = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                                authorities.add(new SimpleGrantedAuthority(authority));
                            }
                        }

                        @SuppressWarnings("unchecked")
                        List<String> permissions = claims.get("permissions", List.class);
                        if (permissions != null) {
                            for (String permission : permissions) {
                                authorities.add(new SimpleGrantedAuthority(permission));
                            }
                        }

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(user, null, authorities);
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("JWT authentication could not be established: requestId={}",
                    org.slf4j.MDC.get("requestId"));
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }
}
