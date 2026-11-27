package com.reboot.identity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Fields that may be changed in the authenticated profile")
public class ProfilePatchRequestDto {

    @Email
    @Size(max = 255)
    private String email;

    @Size(min = 1, max = 255)
    private String fullName;
}
