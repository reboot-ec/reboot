package com.reboot.identity.controller;

import com.reboot.identity.dto.ApiMetaDto;
import com.reboot.identity.dto.ApiResponseDto;
import com.reboot.identity.dto.AuthResponseDto;
import com.reboot.identity.dto.LoginRequestDto;
import com.reboot.identity.dto.LogoutRequestDto;
import com.reboot.identity.dto.MessageResponseDto;
import com.reboot.identity.dto.PasswordResetConfirmationDto;
import com.reboot.identity.dto.PasswordResetRequestDto;
import com.reboot.identity.dto.RefreshRequestDto;
import com.reboot.identity.dto.RegisterRequestDto;
import com.reboot.identity.dto.UserResponseDto;
import com.reboot.identity.model.User;
import com.reboot.identity.service.impl.IdentityAuthService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/identity")
@Tag(name = "Identity authentication", description = "Registration, sign-in and session lifecycle")
public class IdentityAuthController {

    private final IdentityAuthService authService;

    public IdentityAuthController(IdentityAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a local user")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User registered"),
            @ApiResponse(responseCode = "400", description = "Malformed request"),
            @ApiResponse(responseCode = "409", description = "Email or username already exists"),
            @ApiResponse(responseCode = "422", description = "Validation failed"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    public ResponseEntity<ApiResponseDto<UserResponseDto>> register(
            @Valid @RequestBody RegisterRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(success(authService.register(request)));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate using local or Active Directory credentials")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tokens and user profile issued"),
            @ApiResponse(responseCode = "400", description = "Malformed request"),
            @ApiResponse(responseCode = "401", description = "Credentials invalid"),
            @ApiResponse(responseCode = "422", description = "Validation failed"),
            @ApiResponse(responseCode = "500", description = "Authentication provider failure")
    })
    public ApiResponseDto<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        return success(authService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rotate a refresh token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Refresh token rotated"),
            @ApiResponse(responseCode = "400", description = "Malformed request"),
            @ApiResponse(responseCode = "401", description = "Refresh token invalid or expired"),
            @ApiResponse(responseCode = "422", description = "Validation failed"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    public ApiResponseDto<AuthResponseDto> refresh(@Valid @RequestBody RefreshRequestDto request) {
        return success(authService.refresh(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    @Operation(summary = "Revoke the authenticated user's refresh session")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Session revoked"),
            @ApiResponse(responseCode = "401", description = "Authentication or refresh session invalid"),
            @ApiResponse(responseCode = "403", description = "Refresh session belongs to another user"),
            @ApiResponse(responseCode = "422", description = "Validation failed"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    public ApiResponseDto<MessageResponseDto> logout(
            @AuthenticationPrincipal User authenticatedUser,
            @Valid @RequestBody LogoutRequestDto request) {
        authService.logout(authenticatedUser.getId(), request.getRefreshToken());
        return success(new MessageResponseDto("Session revoked."));
    }

    @PostMapping("/password-reset-requests")
    @Operation(summary = "Request a password reset email")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Request accepted without disclosing account existence"),
            @ApiResponse(responseCode = "400", description = "Malformed request"),
            @ApiResponse(responseCode = "422", description = "Validation failed"),
            @ApiResponse(responseCode = "500", description = "Email delivery failed")
    })
    public ResponseEntity<ApiResponseDto<MessageResponseDto>> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequestDto request) {
        authService.requestPasswordReset(request.getEmail());
        return ResponseEntity.accepted()
                .body(success(new MessageResponseDto("If the account is eligible, reset instructions will be sent.")));
    }

    @PostMapping("/password-reset-confirmations")
    @Operation(summary = "Confirm a one-time password reset")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password reset completed"),
            @ApiResponse(responseCode = "400", description = "Malformed request"),
            @ApiResponse(responseCode = "403", description = "Password is managed by Active Directory"),
            @ApiResponse(responseCode = "422", description = "Reset token or password invalid"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    public ApiResponseDto<MessageResponseDto> confirmPasswordReset(
            @Valid @RequestBody PasswordResetConfirmationDto request) {
        authService.confirmPasswordReset(request.getToken(), request.getNewPassword());
        return success(new MessageResponseDto("Password reset completed."));
    }

    private <T> ApiResponseDto<T> success(T data) {
        return new ApiResponseDto<>(data, new ApiMetaDto(MDC.get("requestId")));
    }
}
