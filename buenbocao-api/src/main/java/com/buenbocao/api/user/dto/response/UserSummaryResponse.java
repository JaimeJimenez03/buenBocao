package com.buenbocao.api.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO resumido de un usuario.
 * Se usa en listas de seguidores, resultados de búsqueda, autores de recetas,
 * etc.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryResponse {

    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String profileImage;
    private String bio;

    /** Número de seguidores del autor. */
    @Builder.Default
    private long followersCount = 0;

    /** Número de recetas publicadas del autor. */
    @Builder.Default
    private long recipesCount = 0;

    /**
     * Total de valoraciones recibidas en todas las recetas del autor.
     * Ej: 2 recetas × 2 valoraciones = 4.
     */
    @Builder.Default
    private long totalReviewsReceived = 0;
}