package com.buenbocao.api.user.entity;

import com.buenbocao.api.common.audit.AuditableEntity;
import com.buenbocao.api.common.enums.Role;
import com.buenbocao.api.recipe.entity.Diet;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad principal de usuario.
 * Representa a un usuario registrado en la plataforma Buen Bocao.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(length = 300)
    private String bio;

    @Column(name = "profile_image")
    private String profileImage;

    @Builder.Default
    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @Column(name = "email_verification_token")
    private String emailVerificationToken;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.USER;

    @Builder.Default
    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "username_last_changed_at")
    private LocalDateTime usernameLastChangedAt;

    @Column(name = "expo_push_token", length = 200)
    private String expoPushToken;

    @Column(name = "ban_reason", length = 500)
    private String banReason;

    @Column(name = "ban_expires_at")
    private LocalDateTime banExpiresAt;

    @Column(name = "password_reset_token", length = 10)
    private String passwordResetToken;

    @Column(name = "password_reset_token_expiry")
    private LocalDateTime passwordResetTokenExpiry;

    @Builder.Default
    @ManyToMany
    @JoinTable(name = "user_dietary_preferences", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "diet_id"))
    private Set<Diet> dietaryPreferences = new HashSet<>();
}