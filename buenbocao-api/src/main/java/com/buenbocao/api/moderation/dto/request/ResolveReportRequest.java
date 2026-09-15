package com.buenbocao.api.moderation.dto.request;

import com.buenbocao.api.common.enums.ReportStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ResolveReportRequest {

    @NotNull(message = "El nuevo estado es obligatorio")
    private ReportStatus status;

    @Size(max = 500)
    private String moderatorNotes;
}
