package com.reboot.identity.dto;

import com.reboot.identity.model.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Administrative user status update")
public class StatusUpdateRequestDto {

    @NotNull
    private UserStatus status;
}
