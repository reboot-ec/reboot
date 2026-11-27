package com.reboot.organization.dto;

import com.reboot.organization.model.MembershipRole;
import com.reboot.organization.model.MembershipStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Membership role and/or status update")
public record PatchMembershipRequestDto(
        MembershipRole role,
        MembershipStatus status) {
}
