package com.buenbocao.api.recipe.dto.response;

import com.buenbocao.api.user.dto.response.UserSummaryResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * DTO de respuesta con todos los detalles de una receta.
 * Se usa al abrir una receta individual.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeDetailResponse {

    private Long id;
    private String title;
    private String description;
    private String slug;
    private String difficulty;
    private String typeDish;
    private String mealTime;
    private int servings;
    private int prepTimeMinutes;
    private String status;

    // Autor
    private UserSummaryResponse author;

    // Categoría
    private CategoryResponse category;

    // Contenido
    private List<RecipeIngredientResponse> ingredients;
    private List<StepResponse> steps;
    private List<MediaResponse> media;
    private Set<String> tags;
    private Set<String> diets;

    // Contadores sociales (se rellenarán con el módulo social)
    @Builder.Default
    private long likesCount = 0;

    @Builder.Default
    private long commentsCount = 0;

    @Builder.Default
    private boolean likedByCurrentUser = false;

    @Builder.Default
    private boolean savedByCurrentUser = false;

    private LocalDateTime createdAt;
}