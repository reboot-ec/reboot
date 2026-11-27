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
@Schema(description = "Refresh session to revoke")
public class LogoutRequestDto {

    @NotBlank
    @Size(max = 512)
    @ToString.Exclude
    private String refreshToken;
}
