package com.reboot.identity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Schema(description = "One-time password reset token and new password")
public class PasswordResetConfirmationDto {

    @NotBlank
    @Size(max = 512)
    @ToString.Exclude
    private String token;

    @NotBlank
    @Size(min = 10, max = 72)
    @ToString.Exclude
    private String newPassword;
}
