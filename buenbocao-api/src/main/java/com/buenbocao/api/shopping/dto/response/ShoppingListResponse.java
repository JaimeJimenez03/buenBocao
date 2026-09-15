package com.buenbocao.api.shopping.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de respuesta con el detalle de una lista de la compra.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingListResponse {

    private Long id;
    private String title;
    private boolean personal;
    private List<ShoppingItemResponse> items;
    private int totalItems;
    private int checkedItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
