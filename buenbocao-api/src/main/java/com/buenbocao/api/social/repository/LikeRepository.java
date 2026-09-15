package com.buenbocao.api.social.repository;

import com.buenbocao.api.social.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * Repositorio para la entidad Like.
 */
@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    boolean existsByUserIdAndRecipeId(Long userId, Long recipeId);

    void deleteByUserIdAndRecipeId(Long userId, Long recipeId);

    long countByRecipeId(Long recipeId);

    @Query("SELECT l.recipe.id, COUNT(l) FROM Like l WHERE l.recipe.id IN :recipeIds GROUP BY l.recipe.id")
    List<Object[]> countLikesForRecipes(@Param("recipeIds") Set<Long> recipeIds);
}