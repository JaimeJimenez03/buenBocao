package com.buenbocao.api.user.controller;

import com.buenbocao.api.common.constants.AppConstants;
import com.buenbocao.api.common.response.ApiResponse;
import com.buenbocao.api.common.response.PagedResponse;
import com.buenbocao.api.security.CustomUserDetails;
import com.buenbocao.api.user.dto.request.ChangeEmailRequest;
import com.buenbocao.api.user.dto.request.ChangePasswordRequest;
import com.buenbocao.api.user.dto.request.ChangeUsernameRequest;
import com.buenbocao.api.user.dto.request.DeleteAccountRequest;
import com.buenbocao.api.user.dto.request.UpdateProfileRequest;
import com.buenbocao.api.user.dto.response.UserProfileResponse;
import com.buenbocao.api.user.dto.response.UserSummaryResponse;
import com.buenbocao.api.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller de usuario.
 * Endpoints para gestión del perfil, cuenta y búsqueda de usuarios.
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Gestión de perfil, cuenta y búsqueda de usuarios")
public class UserController {

    private final UserService userService;

    // ==================== PERFIL ====================

    @Operation(summary = "Obtener mi perfil")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        UserProfileResponse profile = userService.getMyProfile(currentUser);
        return ResponseEntity.ok(ApiResponse.ok(profile));
    }

    @Operation(summary = "Obtener perfil público de un usuario")
    @GetMapping("/{username}/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserProfile(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable String username) {

        Long currentUserId = currentUser != null ? currentUser.getId() : null;
        UserProfileResponse profile = userService.getUserProfile(username, currentUserId);
        return ResponseEntity.ok(ApiResponse.ok(profile));
    }

    @Operation(summary = "Actualizar datos del perfil")
    @PutMapping("/me/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody UpdateProfileRequest request) {

        UserProfileResponse profile = userService.updateProfile(currentUser, request);
        return ResponseEntity.ok(ApiResponse.ok("Perfil actualizado", profile));
    }

    @Operation(summary = "Eliminar foto de perfil")
    @DeleteMapping("/me/profile-image")
    public ResponseEntity<ApiResponse<Void>> deleteProfileImage(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        userService.deleteProfileImage(currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Foto de perfil eliminada"));
    }

    // ==================== CUENTA ====================

    @Operation(summary = "Cambiar contraseña")
    @PatchMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody ChangePasswordRequest request) {

        userService.changePassword(currentUser, request);
        return ResponseEntity.ok(ApiResponse.ok("Contraseña actualizada correctamente"));
    }

    @Operation(summary = "Cambiar email (requiere re-verificación)")
    @PatchMapping("/me/email")
    public ResponseEntity<ApiResponse<Void>> changeEmail(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody ChangeEmailRequest request) {

        userService.changeEmail(currentUser, request);
        return ResponseEntity.ok(ApiResponse.ok("Email actualizado. Revisa tu correo para verificarlo."));
    }

    @PatchMapping("/me/username")
    public ResponseEntity<ApiResponse<Map<String, String>>> changeUsername(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody ChangeUsernameRequest request) {

        Map<String, String> tokens = userService.changeUsername(currentUser, request);
        return ResponseEntity.ok(ApiResponse.ok("Nombre de usuario actualizado", tokens));
    }

    @Operation(summary = "Eliminar cuenta")
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody DeleteAccountRequest request) {

        userService.deleteAccount(currentUser, request.getPassword());
        return ResponseEntity.ok(ApiResponse.ok("Cuenta eliminada correctamente"));
    }

    // ==================== BÚSQUEDA ====================

    @Operation(summary = "Buscar usuarios por nombre o username")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PagedResponse<UserSummaryResponse>>> searchUsers(
            @RequestParam String query,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {

        PagedResponse<UserSummaryResponse> results = userService.searchUsers(query, page, size);
        return ResponseEntity.ok(ApiResponse.ok(results));
    }

    @Operation(summary = "Actualizar mis preferencias dietéticas (vegano, sin gluten, etc.)")
    @PutMapping("/me/dietary-preferences")
    public ResponseEntity<ApiResponse<java.util.Set<String>>> updateDietaryPreferences(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestBody java.util.Set<String> dietNames) {

        java.util.Set<String> updated = userService.updateDietaryPreferences(currentUser, dietNames);
        return ResponseEntity.ok(ApiResponse.ok("Preferencias dietéticas actualizadas", updated));
    }

    @Operation(summary = "Obtener mis preferencias dietéticas")
    @GetMapping("/me/dietary-preferences")
    public ResponseEntity<ApiResponse<java.util.Set<String>>> getDietaryPreferences(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        java.util.Set<String> diets = userService.getDietaryPreferences(currentUser);
        return ResponseEntity.ok(ApiResponse.ok(diets));
    }
}