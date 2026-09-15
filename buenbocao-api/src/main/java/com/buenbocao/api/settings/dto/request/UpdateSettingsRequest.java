package com.buenbocao.api.settings.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSettingsRequest {

    @Pattern(regexp = "^(light|dark|system)$", message = "Tema debe ser: light, dark o system")
    private String theme;

    @Pattern(regexp = "^(es|en|ca|gl|eu)$", message = "Idioma no soportado")
    private String language;

    private Boolean notifyNewFollower;
    private Boolean notifyNewReview;
    private Boolean notifyNewFavorite;
    private Boolean notifyEvents;
    private Boolean notifyEmail;

    private Boolean pushNewFollower;
    private Boolean pushNewReview;
    private Boolean pushNewFavorite;
    private Boolean pushEvents;
    private Boolean pushSystem;

    // ---- Push token del dispositivo ----
    @Size(max = 200)
    private String expoPushToken;

    @Pattern(regexp = "^(metric|imperial)$", message = "Sistema de medidas debe ser: metric o imperial")
    private String measurementSystem;

    @Size(max = 100)

    private Boolean publicProfile;
    private Boolean showActivity;
}