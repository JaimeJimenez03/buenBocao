package com.buenbocao.api.shopping.entity;

import com.buenbocao.api.common.enums.MeasurementUnit;
import com.buenbocao.api.recipe.entity.Ingredient;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Elemento dentro de una lista de la compra.
 * Puede estar vinculado a un Ingredient de la BD (reutilizable)
 * o ser un item personalizado con solo nombre (para cosas como "papel de cocina").
 */
@Entity
@Table(name = "shopping_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shopping_list_id", nullable = false)
    private ShoppingList shoppingList;

    /**
     * Ingrediente de la BD (opcional).
     * Si es null, se usa customName como nombre libre.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id")
    private Ingredient ingredient;

    /**
     * Nombre personalizado para items que no están en la BD de ingredientes.
     * Se usa solo cuando ingredient es null.
     */
    @Column(name = "custom_name", length = 100)
    private String customName;

    private Double quantity;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private MeasurementUnit unit;

    @Column(length = 100)
    private String notes;

    @Builder.Default
    @Column(nullable = false)
    private boolean checked = false;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    /**
     * Devuelve el nombre del item, priorizando el ingrediente de la BD.
     */
    public String getDisplayName() {
        if (ingredient != null) {
            return ingredient.getName();
        }
        return customName;
    }
}
