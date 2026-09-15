package com.buenbocao.api.auth.controller;

import com.buenbocao.api.auth.dto.request.ForgotPasswordRequest;
import com.buenbocao.api.auth.dto.request.LoginRequest;
import com.buenbocao.api.auth.dto.request.RefreshTokenRequest;
import com.buenbocao.api.auth.dto.request.RegisterRequest;
import com.buenbocao.api.auth.dto.request.ResetPasswordRequest;
import com.buenbocao.api.auth.dto.response.AuthResponse;
import com.buenbocao.api.auth.service.AuthService;
import com.buenbocao.api.common.response.ApiResponse;
import com.buenbocao.api.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller de autenticación.
 * Endpoints públicos para registro, login, refresh token y verificación de email.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints de registro, login y gestión de tokens")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Registrar un nuevo usuario")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Usuario registrado correctamente", response));
    }

    @Operation(summary = "Iniciar sesión")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login exitoso", response));
    }

    @Operation(summary = "Renovar token de acceso")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.ok("Token renovado", response));
    }

    @Operation(summary = "Verificar email mediante token")
    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.ok("Email verificado correctamente"));
    }

    @Operation(summary = "Reenviar email de verificación")
    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<Void>> resendVerification(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        authService.resendVerificationEmail(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Email de verificación reenviado"));
    }

    @Operation(summary = "Solicitar código de recuperación de contraseña")
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(ApiResponse.ok(
                "Si el email está registrado, recibirás un código de recuperación en breve"));
    }

    @Operation(summary = "Restablecer contraseña con código de 6 dígitos")
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.ok("Contraseña restablecida correctamente"));
    }
}
