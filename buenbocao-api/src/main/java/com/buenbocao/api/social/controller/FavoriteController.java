package com.buenbocao.api.social.controller;

import com.buenbocao.api.common.constants.AppConstants;
import com.buenbocao.api.common.response.ApiResponse;
import com.buenbocao.api.common.response.PagedResponse;
import com.buenbocao.api.security.CustomUserDetails;
import com.buenbocao.api.social.dto.response.FavoriteItemResponse;
import com.buenbocao.api.social.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller de favoritos.
 * Endpoints para añadir/quitar y listar recetas favoritas.
 */
@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
@Tag(name = "Favoritos", description = "Gestión de recetas favoritas")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @Operation(summary = "Añadir o quitar receta de favoritos (toggle)")
    @PostMapping("/{recipeId}")
    public ResponseEntity<ApiResponse<Boolean>> toggleFavorite(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long recipeId) {

        boolean added = favoriteService.toggleFavorite(currentUser, recipeId);
        String message = added ? "Receta añadida a favoritos" : "Receta eliminada de favoritos";
        return ResponseEntity.ok(ApiResponse.ok(message, added));
    }

    @Operation(summary = "Listar mis recetas favoritas")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<PagedResponse<FavoriteItemResponse>>> getMyFavorites(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {

        PagedResponse<FavoriteItemResponse> favorites = favoriteService.getMyFavorites(currentUser, page, size);
        return ResponseEntity.ok(ApiResponse.ok(favorites));
    }
}
