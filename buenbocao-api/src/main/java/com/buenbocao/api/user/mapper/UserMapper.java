package com.buenbocao.api.user.mapper;

import com.buenbocao.api.user.dto.response.UserProfileResponse;
import com.buenbocao.api.user.dto.response.UserSummaryResponse;
import com.buenbocao.api.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper para convertir entre la entidad User y sus DTOs.
 * MapStruct genera la implementación automáticamente en tiempo de compilación.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Convierte un User a su perfil completo.
     * Los contadores sociales se establecen por defecto en 0 (se rellenarán desde
     * el servicio).
     */
    @Mapping(target = "role", expression = "java(user.getRole().name())")
    @Mapping(target = "followersCount", constant = "0L")
    @Mapping(target = "followingCount", constant = "0L")
    @Mapping(target = "recipesCount", constant = "0L")
    @Mapping(target = "totalReviewsReceived", constant = "0L")
    @Mapping(target = "followedByCurrentUser", constant = "false")
    @Mapping(target = "subscriptionPlan", ignore = true)
    UserProfileResponse toProfileResponse(User user);

    /**
     * Convierte un User a su resumen (para listas, búsquedas, etc.).
     * Los contadores sociales se inicializan a 0; se enriquecen desde
     * RecipeEnricher
     * cuando el summary va embebido en una RecipeCardResponse.
     */
    @Mapping(target = "followersCount", constant = "0L")
    @Mapping(target = "recipesCount", constant = "0L")
    @Mapping(target = "totalReviewsReceived", constant = "0L")
    UserSummaryResponse toSummaryResponse(User user);
}