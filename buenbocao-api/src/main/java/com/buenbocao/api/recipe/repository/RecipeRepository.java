package com.buenbocao.api.recipe.repository;

import com.buenbocao.api.common.enums.RecipeStatus;
import com.buenbocao.api.common.enums.TypeDish;
import com.buenbocao.api.recipe.entity.Recipe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

        Optional<Recipe> findBySlug(String slug);

        boolean existsBySlug(String slug);

        Page<Recipe> findByAuthorIdAndStatus(Long authorId, RecipeStatus status, Pageable pageable);

        Page<Recipe> findByAuthorId(Long authorId, Pageable pageable);

        Page<Recipe> findByCategoryIdAndStatus(Long categoryId, RecipeStatus status, Pageable pageable);

        @Query("SELECT r FROM Recipe r WHERE r.status = :status AND " +
                        "(LOWER(r.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
                        "LOWER(r.description) LIKE LOWER(CONCAT('%', :query, '%')))")
        Page<Recipe> searchByTitleOrDescription(
                        @Param("query") String query,
                        @Param("status") RecipeStatus status,
                        Pageable pageable);

        // ── Búsqueda sin filtro de estado (para el panel admin) ──
        @Query("SELECT r FROM Recipe r WHERE " +
                        "LOWER(r.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
                        "LOWER(r.description) LIKE LOWER(CONCAT('%', :query, '%'))")
        Page<Recipe> searchAllByTitleOrDescription(
                        @Param("query") String query,
                        Pageable pageable);

        @Query("SELECT DISTINCT r FROM Recipe r " +
                        "JOIN r.ingredients ri " +
                        "WHERE ri.ingredient.id = :ingredientId AND r.status = :status")
        Page<Recipe> findByIngredientIdAndStatus(
                        @Param("ingredientId") Long ingredientId,
                        @Param("status") RecipeStatus status,
                        Pageable pageable);

        @Query("SELECT r FROM Recipe r JOIN r.tags t WHERE t.id = :tagId AND r.status = :status")
        Page<Recipe> findByTagIdAndStatus(
                        @Param("tagId") Long tagId,
                        @Param("status") RecipeStatus status,
                        Pageable pageable);

        @Query("SELECT r FROM Recipe r JOIN r.diets d WHERE d.id = :dietId AND r.status = :status")
        Page<Recipe> findByDietIdAndStatus(
                        @Param("dietId") Long dietId,
                        @Param("status") RecipeStatus status,
                        Pageable pageable);

        @Query("""
                        SELECT DISTINCT r FROM Recipe r
                        LEFT JOIN r.tags t
                        LEFT JOIN r.diets d
                        WHERE r.status = :status
                        AND (:categoryIds IS NULL OR r.category.id IN :categoryIds)
                        AND (:tagIds IS NULL OR t.id IN :tagIds)
                        AND (:dietIds IS NULL OR d.id IN :dietIds)
                        """)
        Page<Recipe> searchWithFilters(
                        @Param("categoryIds") List<Long> categoryIds,
                        @Param("tagIds") List<Long> tagIds,
                        @Param("dietIds") List<Long> dietIds,
                        @Param("status") RecipeStatus status,
                        Pageable pageable);

        /**
         * Top recetas por popularidad:
         * pondera rating medio (x3), número de reviews (x2) y favoritos (x1).
         * Solo recetas publicadas.
         */
        @Query("""
                        SELECT r FROM Recipe r
                        LEFT JOIN Review rv ON rv.recipe = r
                        LEFT JOIN Favorite fv ON fv.recipe = r
                        WHERE r.status = :status
                        GROUP BY r
                        ORDER BY
                            (COALESCE(AVG(rv.rating), 0) * 3
                            + COUNT(DISTINCT rv) * 2
                            + COUNT(DISTINCT fv) * 1) DESC,
                            r.createdAt DESC
                        """)
        Page<Recipe> findTopRecipesByPopularity(
                        @Param("status") RecipeStatus status,
                        Pageable pageable);

        @Query("""
                        SELECT r FROM Recipe r
                        LEFT JOIN Review rv ON rv.recipe = r
                        LEFT JOIN Favorite fv ON fv.recipe = r
                        WHERE r.status = :status
                        AND (:typeDish IS NULL OR r.typeDish = :typeDish)
                        GROUP BY r
                        ORDER BY
                            (COALESCE(AVG(rv.rating), 0) * 3
                            + COUNT(DISTINCT rv) * 2
                            + COUNT(DISTINCT fv) * 1) DESC,
                            r.createdAt DESC
                        """)
        Page<Recipe> findTopRecipesByPopularityFiltered(
                        @Param("status") RecipeStatus status,
                        @Param("typeDish") TypeDish typeDish,
                        Pageable pageable);

        @Query("""
                        SELECT r FROM Recipe r
                        WHERE r.status = :status
                        AND (:typeDish IS NULL OR r.typeDish = :typeDish)
                        ORDER BY r.createdAt DESC
                        """)
        Page<Recipe> findByStatusFiltered(
                        @Param("status") RecipeStatus status,
                        @Param("typeDish") TypeDish typeDish,
                        Pageable pageable);

        Page<Recipe> findByStatus(RecipeStatus status, Pageable pageable);

        /** ARCHIVED sin bloqueo admin: recetas archivadas por el propio usuario. */

        Page<Recipe> findByAuthorIdInAndStatus(List<Long> authorIds, RecipeStatus status, Pageable pageable);

        long countByAuthorIdAndStatus(Long authorId, RecipeStatus status);

        // ── Métodos añadidos para el panel admin ──
        long countByStatus(RecipeStatus status);

}