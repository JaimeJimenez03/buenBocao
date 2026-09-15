package com.buenbocao.api.social.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO con las estadísticas sociales de una receta.
 * Puntuación media, número de reviews, favoritos, y si el usuario actual interactuó.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeStatsResponse {

    private Long recipeId;
    private double averageRating;
    private long reviewsCount;
    private long favoritesCount;
    private boolean favoritedByCurrentUser;
    private boolean reviewedByCurrentUser;
}
