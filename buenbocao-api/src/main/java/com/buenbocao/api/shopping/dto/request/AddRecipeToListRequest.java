package com.buenbocao.api.shopping.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para añadir todos los ingredientes de una receta a una lista de la compra.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddRecipeToListRequest {

    @NotNull(message = "El ID de la receta es obligatorio")
    private Long recipeId;
}
