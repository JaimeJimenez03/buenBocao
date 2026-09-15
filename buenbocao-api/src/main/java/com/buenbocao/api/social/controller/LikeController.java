package com.buenbocao.api.social.controller;

import com.buenbocao.api.common.response.ApiResponse;
import com.buenbocao.api.security.CustomUserDetails;
import com.buenbocao.api.social.service.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller de likes.
 * Endpoints para dar/quitar like y consultar estado.
 */
@RestController
@RequestMapping("/recipes/{recipeId}/likes")
@RequiredArgsConstructor
@Tag(name = "Likes", description = "Gestión de likes en recetas")
public class LikeController {

    private final LikeService likeService;

    @Operation(summary = "Dar o quitar like a una receta (toggle)")
    @PostMapping
    public ResponseEntity<ApiResponse<Boolean>> toggleLike(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long recipeId) {

        boolean liked = likeService.toggleLike(currentUser, recipeId);
        String message = liked ? "Like añadido" : "Like eliminado";
        return ResponseEntity.ok(ApiResponse.ok(message, liked));
    }

    @Operation(summary = "Comprobar si el usuario actual ha dado like a la receta")
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Boolean>> getLikeStatus(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long recipeId) {

        boolean liked = likeService.isLiked(currentUser.getId(), recipeId);
        return ResponseEntity.ok(ApiResponse.ok(liked));
    }

    @Operation(summary = "Obtener el número de likes de una receta")
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getLikesCount(
            @PathVariable Long recipeId) {

        long count = likeService.countLikes(recipeId);
        return ResponseEntity.ok(ApiResponse.ok(count));
    }
}