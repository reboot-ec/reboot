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
@Schema(description = "Credentials and profile details for local registration")
public class RegisterRequestDto {

    @NotBlank
    @Email
    @Size(max = 255)
    @Schema(example = "user@example.com")
    private String email;

    @NotBlank
    @Size(min = 3, max = 150)
    @Schema(example = "reboot-user")
    private String username;

    @NotBlank
    @Size(max = 255)
    @Schema(example = "Reboot User")
    private String fullName;

    @NotBlank
    @Size(min = 10, max = 72)
    @ToString.Exclude
    @Schema(description = "Password, 10 to 72 characters")
    private String password;
}
