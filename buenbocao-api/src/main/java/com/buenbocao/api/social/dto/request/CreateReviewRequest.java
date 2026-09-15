package com.buenbocao.api.social.dto.request;

import com.buenbocao.api.common.constants.AppConstants;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

/**
 * DTO para crear o actualizar una review.
 * La puntuación es obligatoria, el comentario y la media son opcionales.
 * La media se sube primero vía POST /storage/review-media y luego
 * se envían las rutas relativas + tipo en este request.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateReviewRequest {

    @NotNull(message = "La puntuación es obligatoria")
    @Min(value = 1, message = "La puntuación mínima es 1")
    @Max(value = 5, message = "La puntuación máxima es 5")
    private Integer rating;

    @Size(max = AppConstants.COMMENT_MAX_LENGTH,
          message = "El comentario no puede superar los {max} caracteres")
    private String comment;

    /** Lista de media adjunta (máximo 5). Puede ser null o vacía. */
    @Size(max = 5, message = "Máximo 5 archivos por review")
    private List<MediaItem> media;

    /**
     * Referencia a un archivo ya subido al storage.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MediaItem {
        /** Ruta relativa devuelta por POST /storage/review-media */
        private String mediaUrl;
        /** "IMAGE" o "VIDEO" */
        private String mediaType;
    }
}
