package com.buenbocao.api.social.repository;

import com.buenbocao.api.social.entity.Review;
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
 * Repositorio para la entidad Review.
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByUserIdAndRecipeId(Long userId, Long recipeId);

    boolean existsByUserIdAndRecipeId(Long userId, Long recipeId);

    /**
     * Lista las reviews de una receta (las más recientes primero).
     */
    Page<Review> findByRecipeIdOrderByCreatedAtDesc(Long recipeId, Pageable pageable);

    /**
     * Lista las reviews de un usuario (las más recientes primero).
     */
    Page<Review> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Cuenta el número de reviews de una receta.
     */
    long countByRecipeId(Long recipeId);

    /**
     * Calcula la puntuación media de una receta.
     */
    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM Review r WHERE r.recipe.id = :recipeId")
    double getAverageRatingByRecipeId(@Param("recipeId") Long recipeId);

    /**
     * Cuenta el total de reviews recibidas en todas las recetas de un autor.
     * Útil para mostrar en la card/perfil del usuario.
     */
    @Query("SELECT COUNT(r) FROM Review r WHERE r.recipe.author.id = :authorId")
    long countTotalReviewsByAuthorId(@Param("authorId") Long authorId);

    @Query("SELECT r.recipe.id, COUNT(r), AVG(r.rating) FROM Review r WHERE r.recipe.id IN :recipeIds GROUP BY r.recipe.id")
    List<Object[]> getStatsForRecipes(@Param("recipeIds") Set<Long> recipeIds);
}