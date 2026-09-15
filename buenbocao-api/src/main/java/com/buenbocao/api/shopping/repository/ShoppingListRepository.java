package com.buenbocao.api.shopping.repository;

import com.buenbocao.api.shopping.entity.ShoppingList;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para la entidad ShoppingList.
 */
@Repository
public interface ShoppingListRepository extends JpaRepository<ShoppingList, Long> {

    /**
     * Listas de la compra de un usuario, ordenadas por las más recientes.
     */
    Page<ShoppingList> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Cuenta las listas de la compra de un usuario.
     */
    long countByUserId(Long userId);
}
