package com.buenbocao.api.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DTO de respuesta con el perfil completo de un usuario.
 * Se usa para ver el perfil propio o el de otro usuario.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String bio;
    private String profileImage;
    private boolean emailVerified;
    private String subscriptionPlan;
    private String role;

    // Contadores sociales (se rellenarán cuando exista el módulo social)
    @Builder.Default
    private long followersCount = 0;

    @Builder.Default
    private long followingCount = 0;

    @Builder.Default
    private long recipesCount = 0;

    /**
     * Total de valoraciones recibidas en todas las recetas del usuario.
     * Ej: si tiene 2 recetas con 2 valoraciones cada una → 4.
     */
    @Builder.Default
    private long totalReviewsReceived = 0;

    // Indica si el usuario que consulta ya sigue a este perfil
    // Solo relevante cuando ves el perfil de otro usuario
    @Builder.Default
    private boolean followedByCurrentUser = false;

    private LocalDateTime createdAt;
}