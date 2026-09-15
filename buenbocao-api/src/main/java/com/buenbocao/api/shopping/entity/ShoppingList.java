package com.buenbocao.api.shopping.entity;

import com.buenbocao.api.common.audit.AuditableEntity;
import com.buenbocao.api.user.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Lista de la compra de un usuario.
 * Puede ser personal (privada) o compartida (visible por otros, futuro).
 */
@Entity
@Table(name = "shopping_lists")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingList extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    /**
     * Si es true, la lista es privada del usuario.
     * Si es false, podría compartirse (funcionalidad futura).
     */
    @Builder.Default
    @Column(nullable = false)
    private boolean personal = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder.Default
    @OneToMany(mappedBy = "shoppingList", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<ShoppingItem> items = new ArrayList<>();

    // ---- Métodos de utilidad ----

    public void addItem(ShoppingItem item) {
        items.add(item);
        item.setShoppingList(this);
    }

    public void clearItems() {
        items.forEach(i -> i.setShoppingList(null));
        items.clear();
    }
}
