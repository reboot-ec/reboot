package com.reboot.organization.controller;

import com.reboot.organization.config.JwtPrincipal;
import com.reboot.organization.dto.ApiMetaDto;
import com.reboot.organization.dto.ApiResponseDto;
import com.reboot.organization.dto.CreateMembershipRequestDto;
import com.reboot.organization.dto.MembershipResponseDto;
import com.reboot.organization.dto.PatchMembershipRequestDto;
import com.reboot.organization.service.MembershipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/members")
@Tag(name = "Organization memberships", description = "Invite and manage organization members")
@SecurityRequirement(name = "bearerAuth")
public class MembershipController {

    private final MembershipService membershipService;

    public MembershipController(MembershipService membershipService) {
        this.membershipService = membershipService;
    }

    @PostMapping
    @Operation(summary = "Invite a user to an organization")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Membership invitation created"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Organization owner or administrator required"),
            @ApiResponse(responseCode = "404", description = "Organization not found"),
            @ApiResponse(responseCode = "409", description = "User already has an active or pending membership"),
            @ApiResponse(responseCode = "422", description = "Validation failed"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    public ResponseEntity<ApiResponseDto<MembershipResponseDto>> addMember(
            @Parameter(description = "Organization UUID") @PathVariable UUID organizationId,
            @AuthenticationPrincipal JwtPrincipal principal,
            @Valid @RequestBody CreateMembershipRequestDto request) {
        MembershipResponseDto created = membershipService.addMember(
                organizationId, principal.userId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(success(created));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List organization memberships")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Memberships returned"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Active organization membership required"),
            @ApiResponse(responseCode = "404", description = "Organization not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    public ApiResponseDto<List<MembershipResponseDto>> getMembers(
            @Parameter(description = "Organization UUID") @PathVariable UUID organizationId,
            @AuthenticationPrincipal JwtPrincipal principal,
            Authentication authentication) {
        return success(membershipService.getMembers(organizationId, principal.userId(),
                isSystemAdmin(authentication)));
    }

    @PatchMapping("/{memberId}")
    @Operation(summary = "Update a member role or status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Membership updated"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Organization administrator or owner required"),
            @ApiResponse(responseCode = "404", description = "Organization or membership not found"),
            @ApiResponse(responseCode = "422", description = "The change would leave no active owner"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    public ApiResponseDto<MembershipResponseDto> updateMember(
            @Parameter(description = "Organization UUID") @PathVariable UUID organizationId,
            @Parameter(description = "Membership UUID") @PathVariable UUID memberId,
            @AuthenticationPrincipal JwtPrincipal principal,
            @Valid @RequestBody PatchMembershipRequestDto request) {
        return success(membershipService.updateMember(
                organizationId, memberId, principal.userId(), request));
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
