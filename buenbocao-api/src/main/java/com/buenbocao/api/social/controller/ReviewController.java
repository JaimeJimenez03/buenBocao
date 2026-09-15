package com.buenbocao.api.social.controller;

import com.buenbocao.api.common.constants.AppConstants;
import com.buenbocao.api.common.response.ApiResponse;
import com.buenbocao.api.common.response.PagedResponse;
import com.buenbocao.api.notification.service.NotificationService;
import com.buenbocao.api.security.CustomUserDetails;
import com.buenbocao.api.social.dto.request.CreateReviewRequest;
import com.buenbocao.api.social.dto.response.RecipeStatsResponse;
import com.buenbocao.api.social.dto.response.ReviewResponse;
import com.buenbocao.api.social.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Puntuaciones y comentarios de recetas")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "Crear una review en una receta")
    @PostMapping("/recipes/{recipeId}/reviews")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long recipeId,
            @Valid @RequestBody CreateReviewRequest request) {

        ReviewResponse review = reviewService.createReview(currentUser, recipeId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Review creada", review));
    }

    @Operation(summary = "Listado de reviews del usuario")
    @GetMapping("/reviews/me")
    public ResponseEntity<ApiResponse<PagedResponse<ReviewResponse>>> getMyFavorites(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {

        PagedResponse<ReviewResponse> response = reviewService.getMyReviews(currentUser, page, size);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "Actualizar mi review en una receta")
    @PutMapping("/review/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long reviewId,
            @Valid @RequestBody CreateReviewRequest request) {

        ReviewResponse review = reviewService.updateReview(currentUser, reviewId, request);
        return ResponseEntity.ok(ApiResponse.ok("Review actualizada", review));
    }

    @Operation(summary = "Eliminar mi review de una receta")
    @DeleteMapping("/recipes/{recipeId}/reviews")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long recipeId) {

        reviewService.deleteReview(currentUser, recipeId);
        return ResponseEntity.ok(ApiResponse.ok("Review eliminada"));
    }

    @Operation(summary = "Eliminar mi review de una receta pero buscando por id")
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReviewById(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long reviewId) {

        reviewService.deleteReviewById(currentUser, reviewId);
        return ResponseEntity.ok(ApiResponse.ok("Review eliminada"));
    }

    @Operation(summary = "Listar reviews de una receta")
    @GetMapping("/recipes/{recipeId}/reviews")
    public ResponseEntity<ApiResponse<PagedResponse<ReviewResponse>>> getReviews(
            @PathVariable Long recipeId,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {

        PagedResponse<ReviewResponse> reviews = reviewService.getReviewsByRecipe(recipeId, page, size);
        return ResponseEntity.ok(ApiResponse.ok(reviews));
    }

    @Operation(summary = "Obtener estadísticas sociales de una receta")
    @GetMapping("/recipes/{recipeId}/reviews/stats")
    public ResponseEntity<ApiResponse<RecipeStatsResponse>> getRecipeStats(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long recipeId) {

        Long userId = currentUser != null ? currentUser.getId() : null;
        RecipeStatsResponse stats = reviewService.getRecipeStats(recipeId, userId);
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }
}