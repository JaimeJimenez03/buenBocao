package com.buenbocao.api.recipe.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tipo de dieta (Vegana, Vegetariana, Keto, Sin gluten...).
 * Relación many-to-many con Recipe.
 * Una receta puede tener varias dietas.
 */
@Entity
@Table(name = "diets")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Diet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 100)
    private String slug;
}
