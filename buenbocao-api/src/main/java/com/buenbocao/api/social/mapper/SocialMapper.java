package com.buenbocao.api.social.mapper;

import com.buenbocao.api.social.dto.response.ReviewMediaResponse;
import com.buenbocao.api.social.dto.response.ReviewResponse;
import com.buenbocao.api.social.entity.Review;
import com.buenbocao.api.social.entity.ReviewMedia;
import com.buenbocao.api.user.mapper.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper para convertir entidades sociales a DTOs.
 */
@Mapper(componentModel = "spring", uses = { UserMapper.class })
public interface SocialMapper {

    @Mapping(target = "author", source = "user")
    @Mapping(target = "recipeId", source = "recipe.id")
    @Mapping(target = "media", source = "media")
    ReviewResponse toReviewResponse(Review review);

    @Mapping(target = "mediaType", expression = "java(reviewMedia.getMediaType().name())")
    ReviewMediaResponse toReviewMediaResponse(ReviewMedia reviewMedia);
}
