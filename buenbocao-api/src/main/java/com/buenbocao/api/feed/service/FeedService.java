package com.buenbocao.api.feed.service;

import com.buenbocao.api.common.constants.AppConstants;
import com.buenbocao.api.common.enums.RecipeStatus;
import com.buenbocao.api.common.enums.TypeDish;
import com.buenbocao.api.common.response.PagedResponse;
import com.buenbocao.api.recipe.dto.response.RecipeCardResponse;
import com.buenbocao.api.recipe.entity.Recipe;
import com.buenbocao.api.recipe.mapper.RecipeMapper;
import com.buenbocao.api.recipe.repository.RecipeRepository;
import com.buenbocao.api.recipe.service.RecipeEnricher;
import com.buenbocao.api.security.CustomUserDetails;
import com.buenbocao.api.social.repository.FollowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedService {

    private final RecipeRepository recipeRepository;
    private final FollowRepository followRepository;
    private final RecipeMapper recipeMapper;
    private final RecipeEnricher recipeEnricher;

    /**
     * "Para Ti" — recetas de gente que sigues, ordenadas por fecha.
     * Fallback al top por popularidad si no sigues a nadie.
     */
    @Transactional(readOnly = true)
    public PagedResponse<RecipeCardResponse> getPersonalizedFeed(CustomUserDetails currentUser, int page, int size) {
        size = Math.min(size, AppConstants.MAX_PAGE_SIZE);
        Page<Recipe> recipesPage = null;

        if (currentUser != null) {
            List<Long> followingIds = followRepository
                    .findByFollowerIdOrderByCreatedAtDesc(currentUser.getId(), Pageable.unpaged())
                    .getContent().stream()
                    .map(f -> f.getFollowed().getId())
                    .toList();

            if (!followingIds.isEmpty()) {
                Pageable pageable = PageRequest.of(page, size,
                        Sort.by(Sort.Direction.DESC, AppConstants.DEFAULT_SORT_BY));
                recipesPage = recipeRepository.findByAuthorIdInAndStatus(
                        followingIds, RecipeStatus.PUBLISHED, pageable);
            }
        } else {
            recipesPage = recipeRepository.findTopRecipesByPopularity(
                    RecipeStatus.PUBLISHED, PageRequest.of(page, size));
        }

        Long userId = currentUser != null ? currentUser.getId() : null;
        return toPagedResponse(recipesPage, userId);
    }

    /**
     * "Tendencias" — las mejores recetas del momento por rating + favoritos.
     */
    @Transactional(readOnly = true)
    public PagedResponse<RecipeCardResponse> getDiscoverFeed(
            CustomUserDetails currentUser, int page, int size, TypeDish typeDish) {
        size = Math.min(size, AppConstants.MAX_PAGE_SIZE);
        Page<Recipe> recipesPage = recipeRepository.findTopRecipesByPopularityFiltered(
                RecipeStatus.PUBLISHED, typeDish, PageRequest.of(page, size));
        Long userId = currentUser != null ? currentUser.getId() : null;
        return toPagedResponse(recipesPage, userId);
    }

    /**
     * "Recientes" — las últimas recetas publicadas ordenadas por fecha.
     */
    @Transactional(readOnly = true)
    public PagedResponse<RecipeCardResponse> getRecentFeed(
            CustomUserDetails currentUser, int page, int size, TypeDish typeDish) {
        size = Math.min(size, AppConstants.MAX_PAGE_SIZE);
        Page<Recipe> recipesPage = recipeRepository.findByStatusFiltered(
                RecipeStatus.PUBLISHED, typeDish, PageRequest.of(page, size));
        Long userId = currentUser != null ? currentUser.getId() : null;
        return toPagedResponse(recipesPage, userId);
    }

    // ── privado ───────────────────────────────────────────────────────────────

    private PagedResponse<RecipeCardResponse> toPagedResponse(Page<Recipe> page, Long currentUserId) {
        List<RecipeCardResponse> content = page.getContent().stream()
                .map(recipeMapper::toCardResponse)
                .toList();
        recipeEnricher.enrichAll(content, currentUserId);
        return PagedResponse.of(page, content);
    }
}