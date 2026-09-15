package com.buenbocao.api.shopping.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de respuesta para un item de la lista de la compra.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingItemResponse {

    private Long id;
    private String name;
    private Long ingredientId;
    private Double quantity;
    private String unit;
    private String unitAbbreviation;
    private String notes;
    private boolean checked;
    private int sortOrder;
}
