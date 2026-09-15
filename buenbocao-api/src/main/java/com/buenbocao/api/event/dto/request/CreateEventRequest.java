package com.buenbocao.api.event.dto.request;

import com.buenbocao.api.common.enums.EventType;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateEventRequest {

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 150)
    private String title;

    @NotBlank(message = "La descripción es obligatoria")
    private String description;

    @NotNull(message = "El tipo de evento es obligatorio")
    private EventType type;

    private String coverImage;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDateTime startsAt;

    private LocalDateTime endsAt;

    @Size(max = 300)
    private String location;

    private boolean online = true;

    private Integer maxParticipants;

    private boolean premiumOnly = false;

    @Size(max = 300)
    private String tags;

    private java.math.BigDecimal price;

    @Size(max = 500)
    private String joinUrl;

    private Integer minParticipants;

    private LocalDateTime registrationDeadline;

    private String requirements;
}