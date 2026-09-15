package com.buenbocao.api.recommendation.service;

import com.buenbocao.api.common.constants.AppConstants;
import com.buenbocao.api.common.enums.RecipeStatus;
import com.buenbocao.api.common.response.PagedResponse;
import com.buenbocao.api.recipe.dto.response.RecipeCardResponse;
import com.buenbocao.api.recipe.entity.Diet;
import com.buenbocao.api.recipe.entity.Recipe;
import com.buenbocao.api.recipe.mapper.RecipeMapper;
import com.buenbocao.api.recipe.repository.RecipeRepository;
import com.buenbocao.api.recipe.service.RecipeEnricher;
import com.buenbocao.api.security.CustomUserDetails;
import com.buenbocao.api.social.repository.FavoriteRepository;
import com.buenbocao.api.social.repository.FollowRepository;
import com.buenbocao.api.social.repository.ReviewRepository;
import com.buenbocao.api.user.entity.User;
import com.buenbocao.api.user.repository.UserRepository;
import com.buenbocao.api.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final RecipeRepository recipeRepository;
    private final FavoriteRepository favoriteRepository;
    private final ReviewRepository reviewRepository;
    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final RecipeMapper recipeMapper;
    private final RecipeEnricher recipeEnricher;

    @Transactional(readOnly = true)
    public PagedResponse<RecipeCardResponse> getForYou(CustomUserDetails currentUser, int page, int size) {
        size = Math.min(size, AppConstants.MAX_PAGE_SIZE);
        Long userId = currentUser.getId();

        // 1. Pool de recetas publicadas (excluye las propias)
        List<Recipe> allPublished = recipeRepository
                .findByStatus(RecipeStatus.PUBLISHED, Pageable.unpaged())
                .getContent()
                .stream()
                .filter(r -> !r.getAuthor().getId().equals(userId))
                .collect(Collectors.toList());

        Set<Long> allRecipeIds = allPublished.stream()
                .map(Recipe::getId)
                .collect(Collectors.toSet());

        // 2. Favoritos del usuario
        Set<Long> favoritedIds = favoriteRepository
                .findByUserIdOrderByCreatedAtDesc(userId, Pageable.unpaged())
                .getContent().stream()
                .map(f -> f.getRecipe().getId())
                .collect(Collectors.toSet());

        // 3. Preferencias de categoría — calculado desde el pool ya en memoria
        Map<Long, Long> categoryPreferences = new HashMap<>();
        allPublished.stream()
                .filter(r -> favoritedIds.contains(r.getId()) && r.getCategory() != null)
                .forEach(r -> categoryPreferences.merge(r.getCategory().getId(), 1L, Long::sum));

        // 4. Preferencias dietéticas del usuario
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", userId));
        Set<Long> userDietIds = user.getDietaryPreferences().stream()
                .map(Diet::getId)
                .collect(Collectors.toSet());

        // 5. Segundo grado de seguidos
        Set<Long> followingIds = followRepository
                .findByFollowerIdOrderByCreatedAtDesc(userId, Pageable.unpaged())
                .getContent().stream()
                .map(f -> f.getFollowed().getId())
                .collect(Collectors.toSet());

        Set<Long> secondDegreeIds = new HashSet<>();
        followingIds.forEach(fId -> followRepository.findByFollowerIdOrderByCreatedAtDesc(fId, PageRequest.of(0, 10))
                .getContent().forEach(f -> secondDegreeIds.add(f.getFollowed().getId())));
        secondDegreeIds.removeAll(followingIds);
        secondDegreeIds.remove(userId);

        // 6. Stats en bulk — 2 queries para todas las recetas
        Map<Long, Long> favCountMap = new HashMap<>();
        Map<Long, Long> reviewCountMap = new HashMap<>();
        Map<Long, Double> avgRatingMap = new HashMap<>();

        if (!allRecipeIds.isEmpty()) {
            favoriteRepository.countFavoritesForRecipes(allRecipeIds)
                    .forEach(row -> favCountMap.put((Long) row[0], (Long) row[1]));

            reviewRepository.getStatsForRecipes(allRecipeIds)
                    .forEach(row -> {
                        reviewCountMap.put((Long) row[0], (Long) row[1]);
                        avgRatingMap.put((Long) row[0], (Double) row[2]);
                    });
        }

        // 7. Scoring — sin queries dentro del loop
        List<ScoredRecipe> scored = allPublished.stream()
                .map(recipe -> {
                    double score = 0;

                    if (recipe.getCategory() != null
                            && categoryPreferences.containsKey(recipe.getCategory().getId())) {
                        score += categoryPreferences.get(recipe.getCategory().getId()) * 3;
                    }

                    if (secondDegreeIds.contains(recipe.getAuthor().getId())) {
                        score += 5;
                    }

                    if (!userDietIds.isEmpty() && recipe.getDiets() != null) {
                        long dietMatches = recipe.getDiets().stream()
                                .filter(d -> userDietIds.contains(d.getId()))
                                .count();
                        score += dietMatches * 4;
                    }

                    long favCount = favCountMap.getOrDefault(recipe.getId(), 0L);
                    long reviewCount = reviewCountMap.getOrDefault(recipe.getId(), 0L);
                    double avgRating = avgRatingMap.getOrDefault(recipe.getId(), 0.0);

                    score += (favCount * 1.5) + (reviewCount * 2) + (avgRating * 2);

                    if (favoritedIds.contains(recipe.getId()))
                        score -= 20;

                    score += Math.random() * 3;

                    return new ScoredRecipe(recipe, score);
                })
                .sorted(Comparator.comparingDouble(ScoredRecipe::score).reversed())
                .toList();

        // 8. Paginar
        int start = Math.min(page * size, scored.size());
        int end = Math.min(start + size, scored.size());
        List<RecipeCardResponse> content = scored.subList(start, end).stream()
                .map(sr -> recipeMapper.toCardResponse(sr.recipe()))
                .toList();

        Page<RecipeCardResponse> resultPage = new PageImpl<>(content, PageRequest.of(page, size), scored.size());
        recipeEnricher.enrichAll(content, userId);
        return PagedResponse.of(resultPage);
    }

    private record ScoredRecipe(Recipe recipe, double score) {
    }
}