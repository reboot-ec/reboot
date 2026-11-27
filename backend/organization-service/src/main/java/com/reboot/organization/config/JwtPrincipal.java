package com.reboot.organization.config;

import java.util.UUID;

public record JwtPrincipal(UUID userId, String email) {
}
