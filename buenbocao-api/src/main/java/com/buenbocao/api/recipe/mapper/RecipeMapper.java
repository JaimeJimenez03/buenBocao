package com.buenbocao.api.recipe.mapper;

import com.buenbocao.api.recipe.dto.response.CategoryResponse;
import com.buenbocao.api.recipe.dto.response.MediaResponse;
import com.buenbocao.api.recipe.dto.response.RecipeCardResponse;
import com.buenbocao.api.recipe.dto.response.RecipeDetailResponse;
import com.buenbocao.api.recipe.dto.response.RecipeIngredientResponse;
import com.buenbocao.api.recipe.dto.response.StepResponse;
import com.buenbocao.api.recipe.entity.Category;
import com.buenbocao.api.recipe.entity.Diet;
import com.buenbocao.api.recipe.entity.Recipe;
import com.buenbocao.api.recipe.entity.RecipeIngredient;
import com.buenbocao.api.recipe.entity.RecipeMedia;
import com.buenbocao.api.recipe.entity.Step;
import com.buenbocao.api.recipe.entity.Tag;
import com.buenbocao.api.user.mapper.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre entidades de receta y sus DTOs.
 */
@Mapper(componentModel = "spring", uses = { UserMapper.class })
public interface RecipeMapper {

    // ---- Recipe → RecipeDetailResponse ----

    @Mapping(target = "difficulty", expression = "java(recipe.getDifficulty().name())")
    @Mapping(target = "typeDish", expression = "java(recipe.getTypeDish().name())")
    @Mapping(target = "mealTime", expression = "java(recipe.getMealTime().name())")
    @Mapping(target = "status", expression = "java(recipe.getStatus().name())")
    @Mapping(target = "author", source = "author")
    @Mapping(target = "category", source = "category")
    @Mapping(target = "ingredients", source = "ingredients")
    @Mapping(target = "steps", source = "steps")
    @Mapping(target = "media", source = "media")
    @Mapping(target = "tags", source = "tags", qualifiedByName = "tagsToStrings")
    @Mapping(target = "diets", source = "diets", qualifiedByName = "dietsToStrings")
    @Mapping(target = "likesCount", constant = "0L")
    @Mapping(target = "commentsCount", constant = "0L")
    @Mapping(target = "likedByCurrentUser", constant = "false")
    @Mapping(target = "savedByCurrentUser", constant = "false")
    RecipeDetailResponse toDetailResponse(Recipe recipe);

    // ---- Recipe → RecipeCardResponse ----

    @Mapping(target = "difficulty", expression = "java(recipe.getDifficulty().name())")
    @Mapping(target = "typeDish", expression = "java(recipe.getTypeDish().name())")
    @Mapping(target = "mealTime", expression = "java(recipe.getMealTime().name())")
    @Mapping(target = "status", expression = "java(recipe.getStatus().name())")
    @Mapping(target = "coverImage", source = "media", qualifiedByName = "extractCoverImage")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "author", source = "author")
    @Mapping(target = "likesCount", constant = "0L")
    @Mapping(target = "commentsCount", constant = "0L")
    @Mapping(target = "averageRating", constant = "0.0")
    @Mapping(target = "reviewsCount", constant = "0L")
    @Mapping(target = "likedByCurrentUser", constant = "false")
    @Mapping(target = "savedByCurrentUser", constant = "false")
    RecipeCardResponse toCardResponse(Recipe recipe);

    // ---- Entidades auxiliares ----

    CategoryResponse toCategoryResponse(Category category);

    @Mapping(target = "ingredientId", source = "ingredient.id")
    @Mapping(target = "ingredientName", source = "ingredient.name")
    @Mapping(target = "ingredientImage", source = "ingredient.imageUrl")
    @Mapping(target = "unit", expression = "java(recipeIngredient.getUnit().name())")
    @Mapping(target = "unitAbbreviation", expression = "java(recipeIngredient.getUnit().getAbbreviation())")
    RecipeIngredientResponse toIngredientResponse(RecipeIngredient recipeIngredient);

    StepResponse toStepResponse(Step step);

    @Mapping(target = "mediaType", expression = "java(media.getMediaType().name())")
    MediaResponse toMediaResponse(RecipeMedia media);

    // ---- Métodos de utilidad ----

    @Named("tagsToStrings")
    default Set<String> tagsToStrings(Set<Tag> tags) {
        if (tags == null)
            return Set.of();
        return tags.stream().map(Tag::getName).collect(Collectors.toSet());
    }

    @Named("dietsToStrings")
    default Set<String> dietsToStrings(Set<Diet> diets) {
        if (diets == null)
            return Set.of();
        return diets.stream().map(Diet::getName).collect(Collectors.toSet());
    }

    @Named("extractCoverImage")
    default String extractCoverImage(List<RecipeMedia> media) {
        if (media == null || media.isEmpty())
            return null;
        return media.stream()
                .filter(m -> m.getMediaType() == RecipeMedia.MediaType.IMAGE)
                .findFirst()
                .map(RecipeMedia::getMediaUrl)
                .orElse(null);
    }
}