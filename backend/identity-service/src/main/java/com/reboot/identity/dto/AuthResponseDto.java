package com.reboot.identity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Schema(description = "Access and refresh tokens with the authenticated profile")
public class AuthResponseDto {

    @ToString.Exclude
    @Schema(description = "Bearer access token")
    private String accessToken;

    @ToString.Exclude
    @Schema(description = "Opaque refresh token")
    private String refreshToken;

    @Schema(description = "Access token validity in seconds", example = "900")
    private long expiresIn;

    private UserResponseDto user;
}
