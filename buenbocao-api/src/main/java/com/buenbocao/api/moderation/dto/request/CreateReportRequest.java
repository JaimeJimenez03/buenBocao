package com.buenbocao.api.moderation.dto.request;

import com.buenbocao.api.common.enums.ReportReason;
import com.buenbocao.api.common.enums.ReportTargetType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateReportRequest {

    @NotNull(message = "El tipo de contenido es obligatorio")
    private ReportTargetType targetType;

    @NotNull(message = "El ID del contenido es obligatorio")
    private Long targetId;

    /** Uno o más motivos del reporte. */
    @NotEmpty(message = "Debes indicar al menos un motivo")
    @Size(max = 7, message = "Demasiados motivos")
    private List<ReportReason> reasons;

    @Size(max = 500, message = "La descripción no puede superar los {max} caracteres")
    private String description;
}