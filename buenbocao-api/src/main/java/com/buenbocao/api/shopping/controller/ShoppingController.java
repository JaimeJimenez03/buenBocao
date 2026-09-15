package com.buenbocao.api.shopping.controller;

import com.buenbocao.api.common.constants.AppConstants;
import com.buenbocao.api.common.response.ApiResponse;
import com.buenbocao.api.common.response.PagedResponse;
import com.buenbocao.api.recipe.entity.Ingredient;
import com.buenbocao.api.security.CustomUserDetails;
import com.buenbocao.api.shopping.dto.request.AddRecipeToListRequest;
import com.buenbocao.api.shopping.dto.request.CreateShoppingListRequest;
import com.buenbocao.api.shopping.dto.request.ShoppingItemRequest;
import com.buenbocao.api.shopping.dto.response.ShoppingListResponse;
import com.buenbocao.api.shopping.dto.response.ShoppingListSummaryResponse;
import com.buenbocao.api.shopping.service.ShoppingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller de listas de la compra.
 * Endpoints para gestionar listas y sus items.
 */
@RestController
@RequestMapping("/shopping-lists")
@RequiredArgsConstructor
@Tag(name = "Lista de la compra", description = "Gestión de listas de la compra")
public class ShoppingController {

    private final ShoppingService shoppingService;

    // ==================== CRUD LISTAS ====================

    @Operation(summary = "Crear una lista de la compra")
    @PostMapping
    public ResponseEntity<ApiResponse<ShoppingListResponse>> createList(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody CreateShoppingListRequest request) {

        ShoppingListResponse list = shoppingService.createList(currentUser, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Lista creada", list));
    }

    @Operation(summary = "Obtener una lista de la compra")
    @GetMapping("/{listId}")
    public ResponseEntity<ApiResponse<ShoppingListResponse>> getList(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long listId) {

        ShoppingListResponse list = shoppingService.getList(currentUser, listId);
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @Operation(summary = "Listar mis listas de la compra")
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<ShoppingListSummaryResponse>>> getMyLists(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {

        PagedResponse<ShoppingListSummaryResponse> lists = shoppingService.getMyLists(currentUser, page, size);
        return ResponseEntity.ok(ApiResponse.ok(lists));
    }

    @Operation(summary = "Actualizar una lista de la compra")
    @PutMapping("/{listId}")
    public ResponseEntity<ApiResponse<ShoppingListResponse>> updateList(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long listId,
            @Valid @RequestBody CreateShoppingListRequest request) {

        ShoppingListResponse list = shoppingService.updateList(currentUser, listId, request);
        return ResponseEntity.ok(ApiResponse.ok("Lista actualizada", list));
    }

    @Operation(summary = "Eliminar una lista de la compra")
    @DeleteMapping("/{listId}")
    public ResponseEntity<ApiResponse<Void>> deleteList(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long listId) {

        shoppingService.deleteList(currentUser, listId);
        return ResponseEntity.ok(ApiResponse.ok("Lista eliminada"));
    }

    // ==================== ITEMS ====================

    @Operation(summary = "Añadir un item a la lista")
    @PostMapping("/{listId}/items")
    public ResponseEntity<ApiResponse<ShoppingListResponse>> addItem(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long listId,
            @Valid @RequestBody ShoppingItemRequest request) {

        ShoppingListResponse list = shoppingService.addItem(currentUser, listId, request);
        return ResponseEntity.ok(ApiResponse.ok("Item añadido", list));
    }

    @Operation(summary = "Buscar ingredientes por nombre (autocompletado para lista de la compra)")
    @GetMapping("/ingredients")
    public ResponseEntity<ApiResponse<List<Ingredient>>> searchIngredients(
            @RequestParam(required = false, defaultValue = "") String search) {
        List<Ingredient> ingredients = shoppingService.searchIngredients(search);
        return ResponseEntity.ok(ApiResponse.ok(ingredients));
    }

    @Operation(summary = "Marcar/desmarcar item como comprado (toggle)")
    @PatchMapping("/{listId}/items/{itemId}/toggle")
    public ResponseEntity<ApiResponse<ShoppingListResponse>> toggleItem(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long listId,
            @PathVariable Long itemId) {

        ShoppingListResponse list = shoppingService.toggleItem(currentUser, listId, itemId);
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @Operation(summary = "Eliminar un item de la lista")
    @DeleteMapping("/{listId}/items/{itemId}")
    public ResponseEntity<ApiResponse<ShoppingListResponse>> removeItem(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long listId,
            @PathVariable Long itemId) {

        ShoppingListResponse list = shoppingService.removeItem(currentUser, listId, itemId);
        return ResponseEntity.ok(ApiResponse.ok("Item eliminado", list));
    }

    // ==================== RECETAS ====================

    @Operation(summary = "Añadir ingredientes de una receta a la lista")
    @PostMapping("/{listId}/from-recipe")
    public ResponseEntity<ApiResponse<ShoppingListResponse>> addRecipeIngredients(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long listId,
            @Valid @RequestBody AddRecipeToListRequest request) {

        ShoppingListResponse list = shoppingService.addRecipeIngredients(currentUser, listId, request.getRecipeId());
        return ResponseEntity.ok(ApiResponse.ok("Ingredientes de la receta añadidos", list));
    }
}
