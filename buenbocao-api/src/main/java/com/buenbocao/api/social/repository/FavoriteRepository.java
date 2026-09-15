package com.buenbocao.api.social.repository;

import com.buenbocao.api.social.entity.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Repositorio para la entidad Favorite.
 */
@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Optional<Favorite> findByUserIdAndRecipeId(Long userId, Long recipeId);

    boolean existsByUserIdAndRecipeId(Long userId, Long recipeId);

    /**
     * Lista los favoritos de un usuario (los más recientes primero).
     */
    Page<Favorite> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Lista los favoritos de un usuario (los primeros añadidos primero).
     * Usado para mostrar los 5 primeros como libres en plan gratuito.
     */
    Page<Favorite> findByUserIdOrderByCreatedAtAsc(Long userId, Pageable pageable);

    /**
     * Cuenta el total de favoritos de un usuario.
     */
    long countByUserId(Long userId);

    /**
     * Cuenta cuántos usuarios han guardado una receta en favoritos.
     */
    long countByRecipeId(Long recipeId);

    void deleteByUserIdAndRecipeId(Long userId, Long recipeId);

    @Query("SELECT f.recipe.id, COUNT(f) FROM Favorite f WHERE f.recipe.id IN :recipeIds GROUP BY f.recipe.id")
    List<Object[]> countFavoritesForRecipes(@Param("recipeIds") Set<Long> recipeIds);
}
