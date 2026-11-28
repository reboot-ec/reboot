package com.reboot.organization.controller;

import com.reboot.organization.config.JwtPrincipal;
import com.reboot.organization.dto.ApiMetaDto;
import com.reboot.organization.dto.ApiResponseDto;
import com.reboot.organization.dto.CreateOrganizationRequestDto;
import com.reboot.organization.dto.OrganizationPageDto;
import com.reboot.organization.dto.OrganizationResponseDto;
import com.reboot.organization.dto.PatchOrganizationRequestDto;
import com.reboot.organization.model.OrganizationType;
import com.reboot.organization.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/v1/organizations")
@Tag(name = "Organizations", description = "Organization and membership operations")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create an organization and become its active owner")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Organization created"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "409", description = "Organization already exists"),
            @ApiResponse(responseCode = "422", description = "Validation failed"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    public ResponseEntity<ApiResponseDto<OrganizationResponseDto>> create(
            @AuthenticationPrincipal JwtPrincipal principal,
            @Valid @RequestBody CreateOrganizationRequestDto request) {
        OrganizationResponseDto created = organizationService.create(principal.userId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(success(created));
    }

    @GetMapping
    @Operation(summary = "Search organizations with pagination")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Organizations returned"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination or filter"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    public ApiResponseDto<OrganizationPageDto> getOrganizations(
            @Parameter(description = "Zero-based page number") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size, from 1 to 100") @RequestParam(defaultValue = "20")
            @Min(1) @Max(100) int pageSize,
            @Parameter(description = "Optional organization type") @RequestParam(required = false)
            OrganizationType type,
            @Parameter(description = "Optional case-insensitive name filter") @RequestParam(required = false)
            @Size(max = 255) String name) {
        return success(organizationService.getOrganizations(page, pageSize, type, name));
    }

    @GetMapping("/mine")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "List organizations where the signed-in user has an active membership")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Organizations returned"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ApiResponseDto<OrganizationPageDto> getMyOrganizations(
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int pageSize) {
        return success(organizationService.getMyOrganizations(principal.userId(), page, pageSize));
    }

    @GetMapping("/{organizationId}")
    @Operation(summary = "Get organization details")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Organization returned"),
            @ApiResponse(responseCode = "404", description = "Organization not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    public ApiResponseDto<OrganizationResponseDto> getOrganization(
            @Parameter(description = "Organization UUID") @PathVariable UUID organizationId) {
        return success(organizationService.getOrganization(organizationId));
    }

    @PatchMapping("/{organizationId}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update an organization")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Organization updated"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Owner, administrator, or system administrator required"),
            @ApiResponse(responseCode = "404", description = "Organization not found"),
            @ApiResponse(responseCode = "409", description = "Organization conflicts with an existing resource"),
            @ApiResponse(responseCode = "422", description = "No fields supplied"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    public ApiResponseDto<OrganizationResponseDto> update(
            @Parameter(description = "Organization UUID") @PathVariable UUID organizationId,
            @AuthenticationPrincipal JwtPrincipal principal,
            Authentication authentication,
            @Valid @RequestBody PatchOrganizationRequestDto request) {
        return success(organizationService.update(organizationId, principal.userId(),
                isSystemAdmin(authentication), request));
    }

    private <T> ApiResponseDto<T> success(T data) {
        return new ApiResponseDto<>(data, new ApiMetaDto(MDC.get("requestId")));
    }

    private boolean isSystemAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }
}
