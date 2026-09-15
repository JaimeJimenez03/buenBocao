package com.buenbocao.api.feed.controller;

import com.buenbocao.api.common.constants.AppConstants;
import com.buenbocao.api.common.enums.TypeDish;
import com.buenbocao.api.common.response.ApiResponse;
import com.buenbocao.api.common.response.PagedResponse;
import com.buenbocao.api.feed.service.FeedService;
import com.buenbocao.api.recipe.dto.response.RecipeCardResponse;
import com.buenbocao.api.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/feed")
@RequiredArgsConstructor
@Tag(name = "Feed", description = "Timeline personalizado y descubrimiento")
public class FeedController {

    private final FeedService feedService;

    @Operation(summary = "Feed personalizado (recetas de usuarios que sigues)")
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<RecipeCardResponse>>> getPersonalizedFeed(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                feedService.getPersonalizedFeed(currentUser, page, size)));
    }

    @Operation(summary = "Tendencias — las mejores recetas del momento")
    @GetMapping("/discover")
    public ResponseEntity<ApiResponse<PagedResponse<RecipeCardResponse>>> getDiscoverFeed(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(required = false) TypeDish typeDish) {
        return ResponseEntity.ok(ApiResponse.ok(
                feedService.getDiscoverFeed(currentUser, page, size, typeDish)));
    }

    @Operation(summary = "Recientes — las últimas recetas publicadas")
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<PagedResponse<RecipeCardResponse>>> getRecentFeed(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(required = false) TypeDish typeDish) {
        return ResponseEntity.ok(ApiResponse.ok(
                feedService.getRecentFeed(currentUser, page, size, typeDish)));
    }
}