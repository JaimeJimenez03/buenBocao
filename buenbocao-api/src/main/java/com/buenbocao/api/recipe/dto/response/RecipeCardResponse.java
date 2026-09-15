package com.buenbocao.api.recipe.dto.response;

import com.buenbocao.api.user.dto.response.UserSummaryResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DTO resumido de una receta para el feed y listas.
 * Contiene solo la información necesaria para mostrar una tarjeta.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeCardResponse {

    private Long id;
    private String title;
    private String slug;
    private String description;
    private String difficulty;
    private String typeDish;
    private String mealTime;
    private int servings;
    private int prepTimeMinutes;

    /**
     * URL de la primera imagen (portada).
     */
    private String coverImage;

    /** Estado de la receta: DRAFT, PUBLISHED, ARCHIVED */
    private String status;

    private String categoryName;
    private UserSummaryResponse author;

    @Builder.Default
    private long likesCount = 0;

    @Builder.Default
    private long commentsCount = 0;

    @Builder.Default
    private double averageRating = 0;

    @Builder.Default
    private long reviewsCount = 0;

    @Builder.Default
    private boolean likedByCurrentUser = false;
    private boolean savedByCurrentUser = false;

    private LocalDateTime createdAt;
}