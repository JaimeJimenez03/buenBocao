package com.buenbocao.api.recipe.controller;

import com.buenbocao.api.common.constants.AppConstants;
import com.buenbocao.api.common.enums.RecipeStatus;
import com.buenbocao.api.common.response.ApiResponse;
import com.buenbocao.api.common.response.PagedResponse;
import com.buenbocao.api.recipe.dto.request.CreateRecipeRequest;
import com.buenbocao.api.recipe.dto.response.RecipeCardResponse;
import com.buenbocao.api.recipe.dto.response.RecipeDetailResponse;
import com.buenbocao.api.recipe.service.RecipeService;
import com.buenbocao.api.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller de recetas.
 * Endpoints para crear, editar, eliminar, consultar y filtrar recetas.
 */
@RestController
@RequestMapping("/recipes")
@RequiredArgsConstructor
@Tag(name = "Recetas", description = "Gestión y consulta de recetas")
public class RecipeController {

    private final RecipeService recipeService;

    // ==================== CRUD ====================

    @Operation(summary = "Crear una nueva receta")
    @PostMapping
    public ResponseEntity<ApiResponse<RecipeDetailResponse>> createRecipe(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody CreateRecipeRequest request) {

        RecipeDetailResponse recipe = recipeService.createRecipe(currentUser, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Receta creada", recipe));
    }

    @Operation(summary = "Actualizar una receta")
    @PutMapping("/{recipeId}")
    public ResponseEntity<ApiResponse<RecipeDetailResponse>> updateRecipe(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long recipeId,
            @Valid @RequestBody CreateRecipeRequest request) {

        RecipeDetailResponse recipe = recipeService.updateRecipe(currentUser, recipeId, request);
        return ResponseEntity.ok(ApiResponse.ok("Receta actualizada", recipe));
    }

    @Operation(summary = "Eliminar una receta")
    @DeleteMapping("/{recipeId}")
    public ResponseEntity<ApiResponse<Void>> deleteRecipe(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long recipeId) {

        recipeService.deleteRecipe(currentUser, recipeId);
        return ResponseEntity.ok(ApiResponse.ok("Receta eliminada"));
    }

    @Operation(summary = "Cambiar estado de una receta (publicar, archivar, borrador)")
    @PatchMapping("/{recipeId}/status")
    public ResponseEntity<ApiResponse<RecipeDetailResponse>> changeStatus(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long recipeId,
            @RequestParam RecipeStatus status) {

        RecipeDetailResponse recipe = recipeService.changeStatus(currentUser, recipeId, status);
        return ResponseEntity.ok(ApiResponse.ok("Estado actualizado", recipe));
    }

    // ==================== CONSULTAS ====================

    // RecipeController.java — solo estos dos métodos cambian

    @Operation(summary = "Obtener receta por slug")
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<RecipeDetailResponse>> getRecipeBySlug(
            @AuthenticationPrincipal CustomUserDetails currentUser, // ← AÑADIR
            @PathVariable String slug) {

        RecipeDetailResponse recipe = recipeService.getRecipeBySlug(slug, currentUser);
        return ResponseEntity.ok(ApiResponse.ok(recipe));
    }

    @Operation(summary = "Obtener receta por ID")
    @GetMapping("/{recipeId}")
    public ResponseEntity<ApiResponse<RecipeDetailResponse>> getRecipeById(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long recipeId) {

        RecipeDetailResponse recipe = recipeService.getRecipeById(recipeId, currentUser);
        return ResponseEntity.ok(ApiResponse.ok(recipe));
    }

    @Operation(summary = "Listar recetas publicadas (feed general)")
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<RecipeCardResponse>>> getPublishedRecipes(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {

        PagedResponse<RecipeCardResponse> recipes = recipeService.getPublishedRecipes(currentUser, page, size);
        return ResponseEntity.ok(ApiResponse.ok(recipes));
    }

    @Operation(summary = "Mis recetas (incluye borradores)")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<PagedResponse<RecipeCardResponse>>> getMyRecipes(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {

        PagedResponse<RecipeCardResponse> recipes = recipeService.getMyRecipes(currentUser, page, size);
        return ResponseEntity.ok(ApiResponse.ok(recipes));
    }

    @Operation(summary = "Recetas de un usuario")
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<PagedResponse<RecipeCardResponse>>> getRecipesByUser(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long userId,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {

        PagedResponse<RecipeCardResponse> recipes = recipeService.getRecipesByUser(currentUser, userId, page, size);
        return ResponseEntity.ok(ApiResponse.ok(recipes));
    }

    // ==================== FILTROS ====================

    @Operation(summary = "Buscar recetas con texto y filtros")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PagedResponse<RecipeCardResponse>>> searchRecipes(
            @AuthenticationPrincipal CustomUserDetails currentUser,

            @RequestParam(required = false) String query,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) List<Long> tags,
            @RequestParam(required = false) List<Long> diets,

            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {

        PagedResponse<RecipeCardResponse> recipes = recipeService.searchRecipesWithFilters(
                currentUser,
                query,
                categories,
                tags,
                diets,
                page,
                size);

        return ResponseEntity.ok(ApiResponse.ok(recipes));
    }

    @Operation(summary = "Recetas por categoría")
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<PagedResponse<RecipeCardResponse>>> getByCategory(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {

        PagedResponse<RecipeCardResponse> recipes = recipeService.getRecipesByCategory(currentUser, categoryId, page,
                size);
        return ResponseEntity.ok(ApiResponse.ok(recipes));
    }

    @Operation(summary = "Recetas por ingrediente")
    @GetMapping("/ingredient/{ingredientId}")
    public ResponseEntity<ApiResponse<PagedResponse<RecipeCardResponse>>> getByIngredient(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long ingredientId,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {

        PagedResponse<RecipeCardResponse> recipes = recipeService.getRecipesByIngredient(currentUser, ingredientId,
                page, size);
        return ResponseEntity.ok(ApiResponse.ok(recipes));
    }

    @Operation(summary = "Recetas por tag")
    @GetMapping("/tag/{tagId}")
    public ResponseEntity<ApiResponse<PagedResponse<RecipeCardResponse>>> getByTag(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long tagId,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {

        PagedResponse<RecipeCardResponse> recipes = recipeService.getRecipesByTag(currentUser, tagId, page, size);
        return ResponseEntity.ok(ApiResponse.ok(recipes));
    }

    @Operation(summary = "Recetas por dieta")
    @GetMapping("/diet/{dietId}")
    public ResponseEntity<ApiResponse<PagedResponse<RecipeCardResponse>>> getByDiet(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long dietId,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {

        PagedResponse<RecipeCardResponse> recipes = recipeService.getRecipesByDiet(currentUser, dietId, page, size);
        return ResponseEntity.ok(ApiResponse.ok(recipes));
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "[Admin] Bloquear/desbloquear receta")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    @org.springframework.web.bind.annotation.PostMapping("/{recipeId}/admin-lock")
    public org.springframework.http.ResponseEntity<com.buenbocao.api.common.response.ApiResponse<com.buenbocao.api.recipe.dto.response.RecipeDetailResponse>> adminLockRecipe(
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.buenbocao.api.security.CustomUserDetails admin,
            @org.springframework.web.bind.annotation.PathVariable Long recipeId,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String reason) {
        return org.springframework.http.ResponseEntity.ok(
                com.buenbocao.api.common.response.ApiResponse.ok("Receta bloqueada",
                        recipeService.adminLockRecipe(admin, recipeId, reason)));
    }

}