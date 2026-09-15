package com.buenbocao.api.social.service;

import com.buenbocao.api.common.exception.ResourceNotFoundException;
import com.buenbocao.api.notification.service.NotificationService;
import com.buenbocao.api.recipe.entity.Recipe;
import com.buenbocao.api.recipe.repository.RecipeRepository;
import com.buenbocao.api.security.CustomUserDetails;
import com.buenbocao.api.social.entity.Like;
import com.buenbocao.api.social.repository.LikeRepository;
import com.buenbocao.api.user.entity.User;
import com.buenbocao.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de likes.
 * Permite dar/quitar like a una receta (toggle).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    /**
     * Da o quita like a una receta (toggle).
     * Devuelve true si se añadió el like, false si se eliminó.
     */
    @Transactional
    public boolean toggleLike(CustomUserDetails currentUser, Long recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new ResourceNotFoundException("Receta", "id", recipeId));

        boolean exists = likeRepository.existsByUserIdAndRecipeId(currentUser.getId(), recipeId);

        if (exists) {
            likeRepository.deleteByUserIdAndRecipeId(currentUser.getId(), recipeId);
            log.info("Like eliminado en receta {} por usuario {}", recipeId, currentUser.getUsername());
            return false;
        } else {
            User user = userRepository.findById(currentUser.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", currentUser.getId()));

            Like like = Like.builder()
                    .user(user)
                    .recipe(recipe)
                    .build();

            likeRepository.save(like);
            log.info("Like añadido en receta {} por usuario {}", recipeId, currentUser.getUsername());

            if (!recipe.getAuthor().getId().equals(currentUser.getId())) {
                notificationService.notifyNewLike(user.getId(), recipe.getAuthor().getId(), recipe.getId(),
                        recipe.getTitle());
            }

            return true;
        }
    }

    /**
     * Comprueba si el usuario actual ha dado like a una receta.
     */
    @Transactional(readOnly = true)
    public boolean isLiked(Long userId, Long recipeId) {
        return likeRepository.existsByUserIdAndRecipeId(userId, recipeId);
    }

    /**
     * Cuenta cuántos likes tiene una receta.
     */
    @Transactional(readOnly = true)
    public long countLikes(Long recipeId) {
        return likeRepository.countByRecipeId(recipeId);
    }
}