package com.buenbocao.api.shopping.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * DTO para crear una lista de la compra.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateShoppingListRequest {

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 100, message = "El título no puede superar los {max} caracteres")
    private String title;

    private boolean personal = false;

    /**
     * Items iniciales de la lista (opcional, se pueden añadir después).
     */
    @Valid
    private List<ShoppingItemRequest> items;
}
