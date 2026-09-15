package com.buenbocao.api.social.service;

import com.buenbocao.api.common.constants.AppConstants;
import com.buenbocao.api.common.enums.SubscriptionStatus;
import com.buenbocao.api.common.exception.BusinessException;
import com.buenbocao.api.common.exception.ResourceNotFoundException;
import com.buenbocao.api.common.response.PagedResponse;
import com.buenbocao.api.notification.service.NotificationService;
import com.buenbocao.api.recipe.dto.response.RecipeCardResponse;
import com.buenbocao.api.recipe.entity.Recipe;
import com.buenbocao.api.recipe.mapper.RecipeMapper;
import com.buenbocao.api.recipe.repository.RecipeRepository;
import com.buenbocao.api.security.CustomUserDetails;
import com.buenbocao.api.social.dto.response.FavoriteItemResponse;
import com.buenbocao.api.social.entity.Favorite;
import com.buenbocao.api.social.repository.FavoriteRepository;
import com.buenbocao.api.subscription.repository.SubscriptionRepository;
import com.buenbocao.api.user.entity.User;
import com.buenbocao.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de favoritos.
 * Permite añadir, quitar y listar recetas favoritas.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteService {

    private static final int FREE_FAVORITES_LIMIT = 5;

    private final FavoriteRepository favoriteRepository;
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    private final RecipeMapper recipeMapper;
    private final NotificationService notificationService;
    private final SubscriptionRepository subscriptionRepository;

    /**
     * Añade una receta a favoritos. Si ya está, la quita (toggle).
     * Devuelve true si se añadió, false si se eliminó.
     */
    @Transactional
    public boolean toggleFavorite(CustomUserDetails currentUser, Long recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new ResourceNotFoundException("Receta", "id", recipeId));

        boolean exists = favoriteRepository.existsByUserIdAndRecipeId(currentUser.getId(), recipeId);

        if (exists) {
            favoriteRepository.deleteByUserIdAndRecipeId(currentUser.getId(), recipeId);
            return false;
        } else {
            // Usuarios sin premium solo pueden guardar hasta FREE_FAVORITES_LIMIT recetas
            boolean isPremium = subscriptionRepository.existsByUserIdAndStatus(
                    currentUser.getId(), SubscriptionStatus.ACTIVE);
            if (!isPremium) {
                long currentCount = favoriteRepository.countByUserId(currentUser.getId());
                if (currentCount >= FREE_FAVORITES_LIMIT) {
                    throw new BusinessException(
                            "Has alcanzado el límite de " + FREE_FAVORITES_LIMIT +
                            " recetas guardadas en el plan gratuito. Hazte Premium para guardar recetas ilimitadas.");
                }
            }

            User user = userRepository.findById(currentUser.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", currentUser.getId()));

            Favorite favorite = Favorite.builder()
                    .user(user)
                    .recipe(recipe)
                    .build();

            favoriteRepository.save(favorite);

            notificationService.notifyNewFavorite(
                    user.getId(),
                    recipe.getAuthor().getId(),
                    recipe.getId(),
                    recipe.getTitle());

            return true;
        }
    }

    /**
     * Lista las recetas favoritas del usuario autenticado.
     * - Premium: ordenadas por fecha descendente (más recientes primero), todas accesibles.
     * - Gratuito: ordenadas por fecha ascendente (primeras añadidas primero);
     *   a partir de la posición FREE_FAVORITES_LIMIT se marcan como locked.
     */
    @Transactional(readOnly = true)
    public PagedResponse<FavoriteItemResponse> getMyFavorites(CustomUserDetails currentUser, int page, int size) {
        size = Math.min(size, AppConstants.MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, size);

        boolean isPremium = subscriptionRepository.existsByUserIdAndStatus(
                currentUser.getId(), SubscriptionStatus.ACTIVE);

        Page<Favorite> favoritesPage = isPremium
                ? favoriteRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId(), pageable)
                : favoriteRepository.findByUserIdOrderByCreatedAtAsc(currentUser.getId(), pageable);

        List<FavoriteItemResponse> content = new ArrayList<>();
        List<Favorite> items = favoritesPage.getContent();
        for (int i = 0; i < items.size(); i++) {
            int absoluteIndex = page * size + i;
            boolean locked = !isPremium && absoluteIndex >= FREE_FAVORITES_LIMIT;
            content.add(FavoriteItemResponse.builder()
                    .recipe(recipeMapper.toCardResponse(items.get(i).getRecipe()))
                    .locked(locked)
                    .build());
        }

        return PagedResponse.of(favoritesPage, content);
    }

    /**
     * Comprueba si el usuario actual tiene una receta en favoritos.
     */
    @Transactional(readOnly = true)
    public boolean isFavorite(Long userId, Long recipeId) {
        return favoriteRepository.existsByUserIdAndRecipeId(userId, recipeId);
    }

    /**
     * Cuenta cuántos usuarios han guardado una receta en favoritos.
     */
    @Transactional(readOnly = true)
    public long countFavorites(Long recipeId) {
        return favoriteRepository.countByRecipeId(recipeId);
    }
}
