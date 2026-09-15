package com.buenbocao.api.recommendation.controller;

import com.buenbocao.api.common.constants.AppConstants;
import com.buenbocao.api.common.response.ApiResponse;
import com.buenbocao.api.common.response.PagedResponse;
import com.buenbocao.api.recipe.dto.response.RecipeCardResponse;
import com.buenbocao.api.recommendation.service.RecommendationService;
import com.buenbocao.api.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/feed/for-you")
@RequiredArgsConstructor
@Tag(name = "Para Ti", description = "Feed personalizado con recomendaciones")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @Operation(summary = "Feed 'Para Ti' personalizado")
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<RecipeCardResponse>>> getForYou(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return ResponseEntity.ok(ApiResponse.ok(recommendationService.getForYou(currentUser, page, size)));
    }
}
