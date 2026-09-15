package com.buenbocao.api.recipe.dto.request;

import com.buenbocao.api.common.enums.MeasurementUnit;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para un ingrediente dentro de una receta.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecipeIngredientRequest {

    /**
     * Nombre del ingrediente. Si no existe en BD, se crea automáticamente.
     */
    @NotBlank(message = "El nombre del ingrediente es obligatorio")
    private String ingredientName;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor que 0")
    private Double quantity;

    @NotNull(message = "La unidad de medida es obligatoria")
    private MeasurementUnit unit;

    /**
     * Notas opcionales ("picado fino", "a temperatura ambiente"...).
     */
    private String notes;
}
