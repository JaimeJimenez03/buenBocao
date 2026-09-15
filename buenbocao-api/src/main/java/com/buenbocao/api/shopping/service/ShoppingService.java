package com.buenbocao.api.shopping.service;

import com.buenbocao.api.common.constants.AppConstants;
import com.buenbocao.api.common.exception.BusinessException;
import com.buenbocao.api.common.exception.ResourceNotFoundException;
import com.buenbocao.api.common.exception.UnauthorizedException;
import com.buenbocao.api.common.response.PagedResponse;
import com.buenbocao.api.common.util.SlugUtils;
import com.buenbocao.api.recipe.entity.Ingredient;
import com.buenbocao.api.recipe.entity.Recipe;
import com.buenbocao.api.recipe.entity.RecipeIngredient;
import com.buenbocao.api.recipe.repository.IngredientRepository;
import com.buenbocao.api.recipe.repository.RecipeRepository;
import com.buenbocao.api.security.CustomUserDetails;
import com.buenbocao.api.shopping.dto.request.CreateShoppingListRequest;
import com.buenbocao.api.shopping.dto.request.ShoppingItemRequest;
import com.buenbocao.api.shopping.dto.response.ShoppingListResponse;
import com.buenbocao.api.shopping.dto.response.ShoppingListSummaryResponse;
import com.buenbocao.api.shopping.entity.ShoppingItem;
import com.buenbocao.api.shopping.entity.ShoppingList;
import com.buenbocao.api.shopping.mapper.ShoppingMapper;
import com.buenbocao.api.shopping.repository.ShoppingItemRepository;
import com.buenbocao.api.shopping.repository.ShoppingListRepository;
import com.buenbocao.api.user.entity.User;
import com.buenbocao.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de listas de la compra.
 * Permite crear, editar, eliminar listas y gestionar sus items.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShoppingService {

    private final ShoppingListRepository shoppingListRepository;
    private final ShoppingItemRepository shoppingItemRepository;
    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;
    private final IngredientRepository ingredientRepository;
    private final ShoppingMapper shoppingMapper;

    /**
     * Crea una nueva lista de la compra.
     */
    @Transactional
    public ShoppingListResponse createList(CustomUserDetails currentUser, CreateShoppingListRequest request) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", currentUser.getId()));

        ShoppingList list = ShoppingList.builder()
                .title(request.getTitle())
                .personal(request.isPersonal())
                .user(user)
                .build();

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            processItems(list, request.getItems());
        }

        ShoppingList saved = shoppingListRepository.save(list);
        log.info("Lista de la compra creada: '{}' por {}", saved.getTitle(), currentUser.getUsername());

        return shoppingMapper.toListResponse(saved);
    }

    /**
     * Obtiene una lista de la compra por ID.
     */
    @Transactional(readOnly = true)
    public ShoppingListResponse getList(CustomUserDetails currentUser, Long listId) {
        ShoppingList list = findListById(listId);
        verifyOwner(list, currentUser);
        return shoppingMapper.toListResponse(list);
    }

    /**
     * Lista todas las listas de la compra del usuario.
     */
    @Transactional(readOnly = true)
    public PagedResponse<ShoppingListSummaryResponse> getMyLists(CustomUserDetails currentUser, int page, int size) {
        size = Math.min(size, AppConstants.MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, size);

        Page<ShoppingList> listsPage = shoppingListRepository
                .findByUserIdOrderByCreatedAtDesc(currentUser.getId(), pageable);

        List<ShoppingListSummaryResponse> content = listsPage.getContent().stream()
                .map(list -> {
                    list.getItems().size();
                    return shoppingMapper.toSummaryResponse(list);
                })
                .toList();

        return PagedResponse.of(listsPage, content);
    }

    /**
     * Actualiza el título de una lista.
     */
    @Transactional
    public ShoppingListResponse updateList(CustomUserDetails currentUser, Long listId,
            CreateShoppingListRequest request) {
        ShoppingList list = findListById(listId);
        verifyOwner(list, currentUser);

        list.setTitle(request.getTitle());
        list.setPersonal(request.isPersonal());

        // Si se envían items, reemplazar todos
        if (request.getItems() != null) {
            list.clearItems();
            processItems(list, request.getItems());
        }

        ShoppingList updated = shoppingListRepository.save(list);
        return shoppingMapper.toListResponse(updated);
    }

    /**
     * Elimina una lista de la compra.
     */
    @Transactional
    public void deleteList(CustomUserDetails currentUser, Long listId) {
        ShoppingList list = findListById(listId);
        verifyOwner(list, currentUser);

        shoppingListRepository.delete(list);
        log.info("Lista '{}' eliminada por {}", list.getTitle(), currentUser.getUsername());
    }

    /**
     * Añade un item a una lista existente.
     */
    @Transactional
    public ShoppingListResponse addItem(CustomUserDetails currentUser, Long listId, ShoppingItemRequest request) {
        ShoppingList list = findListById(listId);
        verifyOwner(list, currentUser);

        int nextOrder = list.getItems().size();
        ShoppingItem item = buildItem(request, nextOrder);
        list.addItem(item);

        ShoppingList updated = shoppingListRepository.save(list);
        return shoppingMapper.toListResponse(updated);
    }

    /**
     * Busca ingredientes por nombre (autocompletado para lista de la compra).
     * Solo lectura — no crea ni modifica ingredientes.
     * Devuelve máximo 20 resultados.
     */
    @Transactional
    public List<Ingredient> searchIngredients(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        return ingredientRepository
                .searchByName(query.trim(), PageRequest.of(0, 20))
                .getContent();
    }

    /**
     * Marca o desmarca un item como comprado (toggle).
     */
    @Transactional
    public ShoppingListResponse toggleItem(CustomUserDetails currentUser, Long listId, Long itemId) {
        ShoppingList list = findListById(listId);
        verifyOwner(list, currentUser);

        ShoppingItem item = list.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", itemId));

        item.setChecked(!item.isChecked());
        ShoppingList updated = shoppingListRepository.save(list);

        return shoppingMapper.toListResponse(updated);
    }

    /**
     * Elimina un item de una lista.
     */
    @Transactional
    public ShoppingListResponse removeItem(CustomUserDetails currentUser, Long listId, Long itemId) {
        ShoppingList list = findListById(listId);
        verifyOwner(list, currentUser);

        boolean removed = list.getItems().removeIf(i -> i.getId().equals(itemId));
        if (!removed) {
            throw new ResourceNotFoundException("Item", "id", itemId);
        }

        ShoppingList updated = shoppingListRepository.save(list);
        return shoppingMapper.toListResponse(updated);
    }

    /**
     * Añade todos los ingredientes de una receta a una lista existente.
     */
    @Transactional
    public ShoppingListResponse addRecipeIngredients(CustomUserDetails currentUser, Long listId, Long recipeId) {
        ShoppingList list = findListById(listId);
        verifyOwner(list, currentUser);

        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new ResourceNotFoundException("Receta", "id", recipeId));

        int nextOrder = list.getItems().size();

        for (RecipeIngredient ri : recipe.getIngredients()) {
            ShoppingItem item = ShoppingItem.builder()
                    .ingredient(ri.getIngredient())
                    .quantity(ri.getQuantity())
                    .unit(ri.getUnit())
                    .notes(ri.getNotes())
                    .checked(false)
                    .sortOrder(nextOrder++)
                    .build();
            list.addItem(item);
        }

        ShoppingList updated = shoppingListRepository.save(list);
        log.info("Ingredientes de receta '{}' añadidos a lista '{}'", recipe.getTitle(), list.getTitle());

        return shoppingMapper.toListResponse(updated);
    }

    // ====================================================================================
    // MÉTODOS PRIVADOS
    // ====================================================================================

    private ShoppingList findListById(Long id) {
        return shoppingListRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lista de la compra", "id", id));
    }

    private void verifyOwner(ShoppingList list, CustomUserDetails currentUser) {
        if (!list.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("No eres el propietario de esta lista");
        }
    }

    private void processItems(ShoppingList list, List<ShoppingItemRequest> itemRequests) {
        for (int i = 0; i < itemRequests.size(); i++) {
            ShoppingItem item = buildItem(itemRequests.get(i), i);
            list.addItem(item);
        }
    }

    private ShoppingItem buildItem(ShoppingItemRequest request, int sortOrder) {
        ShoppingItem.ShoppingItemBuilder builder = ShoppingItem.builder()
                .quantity(request.getQuantity())
                .unit(request.getUnit())
                .notes(request.getNotes())
                .checked(false)
                .sortOrder(sortOrder);

        if (request.getIngredientName() != null && !request.getIngredientName().isBlank()) {
            // Buscar o crear ingrediente en BD
            Ingredient ingredient = ingredientRepository.findByNameIgnoreCase(request.getIngredientName())
                    .orElseGet(() -> ingredientRepository.save(
                            Ingredient.builder()
                                    .name(request.getIngredientName())
                                    .slug(SlugUtils.toSlug(request.getIngredientName()))
                                    .build()));
            builder.ingredient(ingredient);
        } else if (request.getCustomName() != null && !request.getCustomName().isBlank()) {
            builder.customName(request.getCustomName());
        } else {
            throw new BusinessException("Cada item debe tener un ingredientName o un customName");
        }

        return builder.build();
    }
}
