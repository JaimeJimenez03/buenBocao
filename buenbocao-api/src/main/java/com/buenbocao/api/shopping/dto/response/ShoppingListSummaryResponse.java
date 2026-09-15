package com.buenbocao.api.shopping.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DTO resumido de una lista de la compra (para listados).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingListSummaryResponse {

    private Long id;
    private String title;
    private boolean personal;
    private int totalItems;
    private int checkedItems;
    private LocalDateTime createdAt;

    public int getCheckedCount() {
        return checkedItems;
    }
}