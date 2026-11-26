package com.reboot.identity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Schema(description = "Email address for a password reset request")
public class PasswordResetRequestDto {

    @NotBlank
    @Email
    @Size(max = 255)
    private String email;
}
