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
@Schema(description = "Refresh session request")
public class RefreshRequestDto {

    @NotBlank
    @Size(max = 512)
    @ToString.Exclude
    @Schema(description = "Opaque refresh token")
    private String refreshToken;
}
