package com.buenbocao.api.social.controller;

import com.buenbocao.api.common.constants.AppConstants;
import com.buenbocao.api.common.response.ApiResponse;
import com.buenbocao.api.common.response.PagedResponse;
import com.buenbocao.api.security.CustomUserDetails;
import com.buenbocao.api.social.service.FollowService;
import com.buenbocao.api.user.dto.response.UserSummaryResponse;
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
 * Controller de seguidores/seguidos.
 * Endpoints para seguir, dejar de seguir y listar relaciones.
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Seguidores", description = "Gestión de seguidores y seguidos")
public class FollowController {

    private final FollowService followService;

    @Operation(summary = "Seguir o dejar de seguir a un usuario (toggle)")
    @PostMapping("/{userId}/follow")
    public ResponseEntity<ApiResponse<Boolean>> toggleFollow(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long userId) {

        boolean followed = followService.toggleFollow(currentUser, userId);
        String message = followed ? "Ahora sigues a este usuario" : "Has dejado de seguir a este usuario";
        return ResponseEntity.ok(ApiResponse.ok(message, followed));
    }

    @Operation(summary = "Listar seguidores de un usuario")
    @GetMapping("/{userId}/followers")
    public ResponseEntity<ApiResponse<PagedResponse<UserSummaryResponse>>> getFollowers(
            @PathVariable Long userId,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {

        PagedResponse<UserSummaryResponse> followers = followService.getFollowers(userId, page, size);
        return ResponseEntity.ok(ApiResponse.ok(followers));
    }

    @Operation(summary = "Listar usuarios que sigue un usuario")
    @GetMapping("/{userId}/following")
    public ResponseEntity<ApiResponse<PagedResponse<UserSummaryResponse>>> getFollowing(
            @PathVariable Long userId,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {

        PagedResponse<UserSummaryResponse> following = followService.getFollowing(userId, page, size);
        return ResponseEntity.ok(ApiResponse.ok(following));
    }

    @Operation(summary = "Comprobar si sigues a un usuario")
    @GetMapping("/{userId}/follow/status")
    public ResponseEntity<ApiResponse<Boolean>> isFollowing(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long userId) {

        boolean following = followService.isFollowing(currentUser.getId(), userId);
        return ResponseEntity.ok(ApiResponse.ok(following));
    }
}
