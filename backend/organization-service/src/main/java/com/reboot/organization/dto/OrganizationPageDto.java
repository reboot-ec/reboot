package com.reboot.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Paginated organization results")
public record OrganizationPageDto(
        List<OrganizationResponseDto> items,
        int page,
        int pageSize,
        long totalElements,
        int totalPages) {
}
