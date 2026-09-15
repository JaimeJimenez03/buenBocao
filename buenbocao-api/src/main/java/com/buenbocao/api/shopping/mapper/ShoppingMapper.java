package com.buenbocao.api.shopping.mapper;

import com.buenbocao.api.shopping.dto.response.ShoppingItemResponse;
import com.buenbocao.api.shopping.dto.response.ShoppingListResponse;
import com.buenbocao.api.shopping.dto.response.ShoppingListSummaryResponse;
import com.buenbocao.api.shopping.entity.ShoppingItem;
import com.buenbocao.api.shopping.entity.ShoppingList;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

/**
 * Mapper para convertir entidades de shopping a DTOs.
 */
@Mapper(componentModel = "spring")
public interface ShoppingMapper {

    @Mapping(target = "items", source = "items")
    @Mapping(target = "totalItems", expression = "java(shoppingList.getItems().size())")
    @Mapping(target = "checkedItems", source = "items", qualifiedByName = "countChecked")
    ShoppingListResponse toListResponse(ShoppingList shoppingList);

    @Mapping(target = "totalItems", expression = "java(shoppingList.getItems().size())")
    @Mapping(target = "checkedItems", source = "items", qualifiedByName = "countChecked")
    ShoppingListSummaryResponse toSummaryResponse(ShoppingList shoppingList);

    @Mapping(target = "name", expression = "java(item.getDisplayName())")
    @Mapping(target = "ingredientId", source = "ingredient.id")
    @Mapping(target = "unit", expression = "java(item.getUnit() != null ? item.getUnit().name() : null)")
    @Mapping(target = "unitAbbreviation", expression = "java(item.getUnit() != null ? item.getUnit().getAbbreviation() : null)")
    ShoppingItemResponse toItemResponse(ShoppingItem item);

    @Named("countChecked")
    default int countChecked(List<ShoppingItem> items) {
        if (items == null)
            return 0;
        return (int) items.stream().filter(ShoppingItem::isChecked).count();
    }
}
