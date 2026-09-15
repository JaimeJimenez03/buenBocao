package com.buenbocao.api.recipe.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de respuesta para un ingrediente dentro de una receta.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeIngredientResponse {

    private Long ingredientId;
    private String ingredientName;
    private String ingredientImage;
    private Double quantity;
    private String unit;
    private String unitAbbreviation;
    private String notes;
    private int sortOrder;
}
