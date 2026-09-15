package com.buenbocao.api.social.entity;

import com.buenbocao.api.common.audit.AuditableEntity;
import com.buenbocao.api.recipe.entity.Recipe;
import com.buenbocao.api.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Review de una receta.
 * Incluye una puntuación obligatoria (1-5), un comentario opcional
 * y opcionalmente media adjunta (imágenes / vídeos).
 * Un usuario solo puede dejar una review por receta.
 */
@Entity
@Table(name = "reviews", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "recipe_id"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    /** Puntuación de 1 a 5. */
    @Column(nullable = false)
    private int rating;

    /** Comentario opcional sobre la receta. */
    @Column(length = 500)
    private String comment;

    /** Indica si la review fue editada después de crearla. */
    @Builder.Default
    @Column(nullable = false)
    private boolean edited = false;

    /** Media adjunta (imágenes / vídeos). Orden dado por sortOrder. */
    @Builder.Default
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC")
    private List<ReviewMedia> media = new ArrayList<>();
}
