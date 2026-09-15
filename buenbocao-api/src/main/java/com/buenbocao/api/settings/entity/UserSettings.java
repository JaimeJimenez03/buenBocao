package com.buenbocao.api.settings.entity;

import com.buenbocao.api.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

/**
 * Preferencias del usuario en la app.
 * Tema, idioma, configuración de notificaciones, unidades, etc.
 */
@Entity
@Table(name = "user_settings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // ---- APARIENCIA ----

    /** Tema de la app: "light", "dark", "system". */
    @Builder.Default
    @Column(nullable = false, length = 10)
    private String theme = "system";

    /** Idioma de la app: "es", "en", "ca", "gl", "eu". */
    @Builder.Default
    @Column(nullable = false, length = 5)
    private String language = "es";

    // ---- NOTIFICACIONES ----

    @Builder.Default
    @Column(name = "notify_new_follower", nullable = false)
    private boolean notifyNewFollower = true;

    @Builder.Default
    @Column(name = "notify_new_review", nullable = false)
    private boolean notifyNewReview = true;

    @Builder.Default
    @Column(name = "notify_new_favorite", nullable = false)
    private boolean notifyNewFavorite = true;

    @Builder.Default
    @Column(name = "notify_events", nullable = false)
    private boolean notifyEvents = true;

    @Builder.Default
    @Column(name = "notify_email", nullable = false)
    private boolean notifyEmail = true;

    // ---- PUSH (notificaciones al móvil) ----

    /** Nuevo seguidor — OBLIGATORIO, no se puede desactivar. */
    @Builder.Default
    @Column(name = "push_new_follower", nullable = false)
    private boolean pushNewFollower = true;

    @Builder.Default
    @Column(name = "push_new_review", nullable = false)
    private boolean pushNewReview = true;

    @Builder.Default
    @Column(name = "push_new_favorite", nullable = false)
    private boolean pushNewFavorite = true;

    @Builder.Default
    @Column(name = "push_events", nullable = false)
    private boolean pushEvents = true;

    @Builder.Default
    @Column(name = "push_system", nullable = false)
    private boolean pushSystem = false;

    // ---- COCINA ----

    /** Sistema de medidas preferido: "metric", "imperial". */
    @Builder.Default
    @Column(name = "measurement_system", nullable = false, length = 10)
    private String measurementSystem = "metric";

    /** Perfil alimentario por defecto (para filtrar recetas). */

    // ---- PRIVACIDAD ----

    /** Si el perfil es público. */
    @Builder.Default
    @Column(name = "public_profile", nullable = false)
    private boolean publicProfile = true;

    /** Si muestra la actividad reciente (favoritos, reviews). */
    @Builder.Default
    @Column(name = "show_activity", nullable = false)
    private boolean showActivity = true;
}