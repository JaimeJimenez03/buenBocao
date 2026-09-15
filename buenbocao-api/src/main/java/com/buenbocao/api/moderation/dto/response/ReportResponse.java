package com.buenbocao.api.moderation.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {

    private Long id;
    private String reporterUsername;
    private String targetType;
    private Long targetId;

    /**
     * Nombre del contenido reportado (título de receta, username de usuario, etc.)
     */
    private String targetName;

    /** Autor/propietario del contenido reportado */
    private String targetAuthorUsername;

    /** Lista de motivos del reporte */
    private List<String> reasons;

    private String description;
    private String status;
    private String moderatorNotes;
    private String resolvedByUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}