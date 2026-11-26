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
@Schema(description = "Current and new local password")
public class PasswordChangeRequestDto {

    @NotBlank
    @Size(max = 72)
    @ToString.Exclude
    private String currentPassword;

    @NotBlank
    @Size(min = 10, max = 72)
    @ToString.Exclude
    private String newPassword;
}
