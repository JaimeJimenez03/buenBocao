package com.buenbocao.api.recipe.service;

import com.buenbocao.api.common.enums.RecipeStatus;
import com.buenbocao.api.recipe.dto.response.RecipeCardResponse;
import com.buenbocao.api.recipe.repository.RecipeRepository;
import com.buenbocao.api.social.repository.FavoriteRepository;
import com.buenbocao.api.social.repository.FollowRepository;
import com.buenbocao.api.social.repository.LikeRepository;
import com.buenbocao.api.social.repository.ReviewRepository;
import com.buenbocao.api.user.dto.response.UserSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Enriquece RecipeCardResponse con datos sociales (ratings, favoritos, likes)
 * y con los contadores del autor (seguidores, recetas, valoraciones totales).
 *
 * <p>
 * Separación de conceptos:
 * <ul>
 * <li><b>likesCount / likedByCurrentUser</b> → tabla {@code likes} (❤️ me
 * gusta)</li>
 * <li><b>savedByCurrentUser</b> → tabla {@code favorites} (🔖 guardado)</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class RecipeEnricher {

    private final ReviewRepository reviewRepository;
    private final FavoriteRepository favoriteRepository;
    private final LikeRepository likeRepository;
    private final FollowRepository followRepository;
    private final RecipeRepository recipeRepository;

    public RecipeCardResponse enrich(RecipeCardResponse card, Long currentUserId) {
        Long recipeId = card.getId();

        // Valoraciones
        card.setAverageRating(Math.round(reviewRepository.getAverageRatingByRecipeId(recipeId) * 10.0) / 10.0);
        card.setReviewsCount(reviewRepository.countByRecipeId(recipeId));

        // Likes
        card.setLikesCount(likeRepository.countByRecipeId(recipeId));

        if (currentUserId != null) {
            card.setLikedByCurrentUser(likeRepository.existsByUserIdAndRecipeId(currentUserId, recipeId));
        } else {
            card.setLikedByCurrentUser(false);
        }

        // Favoritos
        if (currentUserId != null) {
            card.setSavedByCurrentUser(favoriteRepository.existsByUserIdAndRecipeId(currentUserId, recipeId));
        } else {
            card.setSavedByCurrentUser(false);
        }

        // Contadores sociales del autor
        UserSummaryResponse author = card.getAuthor();
        if (author != null && author.getId() != null) {
            Long authorId = author.getId();
            author.setFollowersCount(followRepository.countByFollowedId(authorId));
            author.setRecipesCount(recipeRepository.countByAuthorIdAndStatus(authorId, RecipeStatus.PUBLISHED));
            author.setTotalReviewsReceived(reviewRepository.countTotalReviewsByAuthorId(authorId));
        }

        return card;
    }

    public List<RecipeCardResponse> enrichAll(List<RecipeCardResponse> cards, Long currentUserId) {
        cards.forEach(c -> enrich(c, currentUserId));
        return cards;
    }

    public RecipeCardResponse enrich(RecipeCardResponse card) {
        return enrich(card, null);
    }

    public List<RecipeCardResponse> enrichAll(List<RecipeCardResponse> cards) {
        return enrichAll(cards, null);
    }
}