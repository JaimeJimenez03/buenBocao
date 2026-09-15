package com.buenbocao.api.recipe.dto.request;

import com.buenbocao.api.recipe.entity.RecipeMedia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para una imagen o vídeo dentro de una receta.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MediaRequest {

    @NotBlank(message = "La URL del media es obligatoria")
    private String mediaUrl;

    @NotNull(message = "El tipo de media es obligatorio")
    private RecipeMedia.MediaType mediaType;
}
