package com.buenbocao.api.recipe.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para un paso de elaboración dentro de una receta.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StepRequest {

    @NotBlank(message = "La descripción del paso es obligatoria")
    private String description;

    /**
     * URL de imagen opcional para el paso.
     */
    private String imageUrl;
}
