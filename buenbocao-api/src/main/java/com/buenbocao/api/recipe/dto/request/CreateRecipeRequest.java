package com.buenbocao.api.recipe.dto.request;

import com.buenbocao.api.common.constants.AppConstants;
import com.buenbocao.api.common.enums.Difficulty;
import com.buenbocao.api.common.enums.MealTime;
import com.buenbocao.api.common.enums.RecipeStatus;
import com.buenbocao.api.common.enums.TypeDish;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;

/**
 * DTO para crear o actualizar una receta.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateRecipeRequest {

      @NotBlank(message = "El título es obligatorio")
      @Size(max = AppConstants.RECIPE_TITLE_MAX_LENGTH, message = "El título no puede superar los {max} caracteres")
      private String title;

      @NotBlank(message = "La descripción es obligatoria")
      @Size(max = AppConstants.RECIPE_DESCRIPTION_MAX_LENGTH, message = "La descripción no puede superar los {max} caracteres")
      private String description;

      @NotNull(message = "La dificultad es obligatoria")
      private Difficulty difficulty;

      @NotNull(message = "El tipo de plato es obligatorio")
      private TypeDish typeDish;

      @NotNull(message = "El mealTime es obligatorio")
      private MealTime mealTime;

      @Min(value = 1, message = "Debe haber al menos 1 comensal")
      private int servings;

      @Min(value = 1, message = "El tiempo de preparación debe ser al menos 1 minuto")
      private int prepTimeMinutes;

      /**
       * Estado inicial de la receta. Por defecto DRAFT.
       * El cliente puede enviar DRAFT o PUBLISHED.
       */
      private RecipeStatus status = RecipeStatus.DRAFT;

      /**
       * ID de la categoría (solo una).
       */
      private Long categoryId;

      /**
       * Lista de ingredientes con cantidad y unidad.
       */
      @NotEmpty(message = "La receta debe tener al menos un ingrediente")
      @Valid
      private List<RecipeIngredientRequest> ingredients;

      /**
       * Pasos de elaboración.
       */
      @NotEmpty(message = "La receta debe tener al menos un paso")
      @Valid
      private List<StepRequest> steps;

      /**
       * URLs de media (imágenes/vídeos).
       */
      private List<MediaRequest> media;

      /**
       * Nombres de tags (se crean si no existen).
       */
      private Set<String> tags;

      /**
       * Nombres de dietas (se crean si no existen).
       */
      private Set<String> diets;
}