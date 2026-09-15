package com.buenbocao.api.recipe.repository;

import com.buenbocao.api.recipe.entity.Diet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la entidad Diet.
 */
@Repository
public interface DietRepository extends JpaRepository<Diet, Long> {

    Optional<Diet> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
