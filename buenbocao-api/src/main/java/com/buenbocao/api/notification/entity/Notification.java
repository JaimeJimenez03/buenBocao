package com.buenbocao.api.notification.entity;

import com.buenbocao.api.common.enums.NotificationType;
import com.buenbocao.api.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Notificación para un usuario.
 */
@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notification_user_read", columnList = "user_id, is_read")
})
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Usuario que recibe la notificación. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    /** Título corto de la notificación. */
    @Column(nullable = false, length = 150)
    private String title;

    /** Mensaje descriptivo. */
    @Column(nullable = false, length = 500)
    private String message;

    /** ID del recurso relacionado (receta, usuario, evento...). */
    @Column(name = "reference_id")
    private Long referenceId;

    /** Tipo del recurso relacionado (recipe, user, event). */
    @Column(name = "reference_type", length = 30)
    private String referenceType;

    /** Username del actor que generó la notificación. */
    @Column(name = "actor_username", length = 30)
    private String actorUsername;

    /** Avatar del actor. */
    @Column(name = "actor_image")
    private String actorImage;

    @Builder.Default
    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
