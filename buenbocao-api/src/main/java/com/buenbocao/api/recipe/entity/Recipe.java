package com.buenbocao.api.recipe.entity;

import com.buenbocao.api.common.audit.AuditableEntity;
import com.buenbocao.api.common.enums.Difficulty;
import com.buenbocao.api.common.enums.MealTime;
import com.buenbocao.api.common.enums.RecipeStatus;
import com.buenbocao.api.common.enums.TypeDish;
import com.buenbocao.api.user.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Entidad principal de receta.
 * Contiene toda la información de una receta publicada en la plataforma.
 */
@Entity
@Table(name = "recipes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Recipe extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, unique = true, length = 200)
    private String slug;

    // ---- Datos de la receta ----

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeDish typeDish;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MealTime mealTime;

    @Column(nullable = false)
    private int servings;

    /**
     * Tiempo de preparación en minutos.
     */
    @Column(name = "prep_time_minutes", nullable = false)
    private int prepTimeMinutes;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private RecipeStatus status = RecipeStatus.DRAFT;

    // ---- Relaciones ----

    /**
     * Usuario creador de la receta.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    /**
     * Categoría de la receta (solo una).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    /**
     * Ingredientes de la receta con cantidades.
     */
    @Builder.Default
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<RecipeIngredient> ingredients = new ArrayList<>();

    /**
     * Pasos de elaboración.
     */
    @Builder.Default
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stepNumber ASC")
    private List<Step> steps = new ArrayList<>();

    /**
     * Imágenes y vídeos de la receta.
     */
    @Builder.Default
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<RecipeMedia> media = new ArrayList<>();

    /**
     * Tags de la receta (many-to-many).
     */
    @Builder.Default
    @ManyToMany
    @JoinTable(name = "recipe_tags", joinColumns = @JoinColumn(name = "recipe_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags = new HashSet<>();

    /**
     * Dietas de la receta (many-to-many).
     */
    @Builder.Default
    @ManyToMany
    @JoinTable(name = "recipe_diets", joinColumns = @JoinColumn(name = "recipe_id"), inverseJoinColumns = @JoinColumn(name = "diet_id"))
    private Set<Diet> diets = new HashSet<>();

    // ---- Métodos de utilidad para manejar relaciones bidireccionales ----

    public void addIngredient(RecipeIngredient ingredient) {
        ingredients.add(ingredient);
        ingredient.setRecipe(this);
    }

    public void addStep(Step step) {
        steps.add(step);
        step.setRecipe(this);
    }

    public void addMedia(RecipeMedia recipeMedia) {
        media.add(recipeMedia);
        recipeMedia.setRecipe(this);
    }

    public void clearIngredients() {
        ingredients.forEach(i -> i.setRecipe(null));
        ingredients.clear();
    }

    public void clearSteps() {
        steps.forEach(s -> s.setRecipe(null));
        steps.clear();
    }

    public void clearMedia() {
        media.forEach(m -> m.setRecipe(null));
        media.clear();
    }
}