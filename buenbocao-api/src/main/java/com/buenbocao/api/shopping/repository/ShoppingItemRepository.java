package com.buenbocao.api.shopping.repository;

import com.buenbocao.api.shopping.entity.ShoppingItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para la entidad ShoppingItem.
 */
@Repository
public interface ShoppingItemRepository extends JpaRepository<ShoppingItem, Long> {
}
