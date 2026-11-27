package com.reboot.identity.controller;

import com.reboot.identity.dto.ApiMetaDto;
import com.reboot.identity.dto.ApiResponseDto;
import com.reboot.identity.dto.MessageResponseDto;
import com.reboot.identity.dto.PasswordChangeRequestDto;
import com.reboot.identity.dto.ProfilePatchRequestDto;
import com.reboot.identity.dto.StatusUpdateRequestDto;
import com.reboot.identity.dto.UserResponseDto;
import com.reboot.identity.model.User;
import com.reboot.identity.service.impl.IdentityUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.MDC;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/identity")
@Tag(name = "Identity users", description = "Current profile and administrative user operations")
@SecurityRequirement(name = "bearerAuth")
public class IdentityUserController {

    private final IdentityUserService userService;

    public IdentityUserController(IdentityUserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @Operation(summary = "Get the authenticated user's profile")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile returned"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    public ApiResponseDto<UserResponseDto> getMe(@AuthenticationPrincipal User authenticatedUser) {
        return success(userService.getMe(authenticatedUser.getId()));
    }

    @PatchMapping("/me")
    @Operation(summary = "Update the authenticated user's email or full name")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated"),
            @ApiResponse(responseCode = "400", description = "Malformed request"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "409", description = "Email already in use"),
            @ApiResponse(responseCode = "422", description = "Validation failed"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    public ApiResponseDto<UserResponseDto> updateMe(
            @AuthenticationPrincipal User authenticatedUser,
            @Valid @RequestBody ProfilePatchRequestDto request) {
        return success(userService.updateMe(authenticatedUser.getId(), request));
    }

    @PatchMapping("/me/password")
    @Operation(summary = "Change the authenticated user's local password")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password changed"),
            @ApiResponse(responseCode = "401", description = "Authentication or current password invalid"),
            @ApiResponse(responseCode = "403", description = "Password is managed by Active Directory"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "422", description = "Validation failed"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    public ApiResponseDto<MessageResponseDto> changePassword(
            @AuthenticationPrincipal User authenticatedUser,
            @Valid @RequestBody PasswordChangeRequestDto request) {
        userService.changePassword(authenticatedUser.getId(), request);
        return success(new MessageResponseDto("Password changed."));
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get a user by ID (administrators only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User returned"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Administrator role required"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    public ApiResponseDto<UserResponseDto> getUser(
            @Parameter(description = "User UUID") @PathVariable UUID userId) {
        return success(userService.getUser(userId));
    }

    @PatchMapping("/users/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Change a user's status (administrators only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status changed"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Administrator role required"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "422", description = "Validation failed"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    public ApiResponseDto<UserResponseDto> updateStatus(
            @AuthenticationPrincipal User authenticatedAdmin,
            @Parameter(description = "User UUID") @PathVariable UUID userId,
            @Valid @RequestBody StatusUpdateRequestDto request) {
        return success(userService.updateUserStatus(
                authenticatedAdmin.getId(), userId, request.getStatus()));
    }

    private <T> ApiResponseDto<T> success(T data) {
        return new ApiResponseDto<>(data, new ApiMetaDto(MDC.get("requestId")));
    }
}
