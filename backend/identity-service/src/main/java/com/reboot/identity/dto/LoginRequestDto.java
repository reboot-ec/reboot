package com.reboot.identity.dto;

import com.reboot.identity.model.AuthProvider;
import com.reboot.identity.model.ClientType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Schema(description = "Username or email and password")
public class LoginRequestDto {

    @NotBlank
    @Size(max = 255)
    @Schema(example = "reboot-user")
    private String usernameOrEmail;

    @NotBlank
    @Size(max = 72)
    @ToString.Exclude
    private String password;

    @NotNull
    private ClientType clientType = ClientType.WEB;

    @Schema(description = "Authentication provider. Omit for automatic provider selection.")
    private AuthProvider authProvider;

    @Size(max = 255)
    private String deviceInfo;
}
