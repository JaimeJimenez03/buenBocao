package com.buenbocao.api.social.dto.response;

import com.buenbocao.api.recipe.dto.response.RecipeCardResponse;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FavoriteItemResponse {
    private RecipeCardResponse recipe;
    private boolean locked;
}
