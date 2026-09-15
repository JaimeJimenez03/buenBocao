package com.buenbocao.api.social.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.buenbocao.api.common.response.ApiResponse;
import com.buenbocao.api.security.CustomUserDetails;
import com.buenbocao.api.social.dto.response.BrowsingHistoryDTO;
import com.buenbocao.api.social.service.BrowsingHistoryService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/browsing-history")
@RequiredArgsConstructor
public class BrowsingHistoryController {

    private final BrowsingHistoryService browsingHistoryService;

    @GetMapping
    @Operation(summary = "Obtener las últimas búsquedas del usuario")
    public ResponseEntity<ApiResponse<List<BrowsingHistoryDTO>>> getByUser(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        List<BrowsingHistoryDTO> history = browsingHistoryService.getBrowsingHistories(currentUser);
        return ResponseEntity.ok(ApiResponse.ok(history));
    }

    @PostMapping
    @Operation(summary = "Guardar una búsqueda en el historial")
    public ResponseEntity<ApiResponse<Void>> save(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestBody String query) {

        browsingHistoryService.saveBrowsingHistory(currentUser, query);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una búsqueda del historial")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long id) {

        browsingHistoryService.deleteBrowsingHistory(currentUser, id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}