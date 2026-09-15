package com.buenbocao.api.shopping.dto.request;

import com.buenbocao.api.common.enums.MeasurementUnit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para un item dentro de una lista de la compra.
 * Se puede vincular a un ingrediente existente (ingredientName)
 * o poner un nombre libre (customName).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingItemRequest {

    /**
     * Nombre del ingrediente de la BD. Si existe, se vincula.
     * Si no existe y customName es null, se crea como ingrediente nuevo.
     */
    private String ingredientName;

    /**
     * Nombre libre para items que no son ingredientes (ej: "papel de cocina").
     * Solo se usa si ingredientName es null.
     */
    private String customName;

    private Double quantity;

    private MeasurementUnit unit;

    private String notes;
}
