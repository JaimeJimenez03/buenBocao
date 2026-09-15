package com.buenbocao.api.settings.controller;

import com.buenbocao.api.common.response.ApiResponse;
import com.buenbocao.api.security.CustomUserDetails;
import com.buenbocao.api.settings.dto.request.UpdateSettingsRequest;
import com.buenbocao.api.settings.dto.response.UserSettingsResponse;
import com.buenbocao.api.settings.service.SettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/settings")
@RequiredArgsConstructor
@Tag(name = "Ajustes", description = "Preferencias del usuario")
public class SettingsController {

    private final SettingsService settingsService;

    @Operation(summary = "Obtener mis ajustes")
    @GetMapping
    public ResponseEntity<ApiResponse<UserSettingsResponse>> getSettings(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(ApiResponse.ok(settingsService.getSettings(currentUser.getId())));
    }

    @Operation(summary = "Actualizar ajustes (parcial: solo envía los campos que cambias)")
    @PatchMapping
    public ResponseEntity<ApiResponse<UserSettingsResponse>> updateSettings(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody UpdateSettingsRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Ajustes actualizados",
                settingsService.updateSettings(currentUser.getId(), request)));
    }

    @Operation(summary = "Restablecer ajustes por defecto")
    @PostMapping("/reset")
    public ResponseEntity<ApiResponse<UserSettingsResponse>> resetSettings(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(ApiResponse.ok("Ajustes restablecidos",
                settingsService.resetSettings(currentUser.getId())));
    }
}
