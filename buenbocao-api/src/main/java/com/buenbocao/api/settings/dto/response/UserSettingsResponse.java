package com.buenbocao.api.settings.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSettingsResponse {

    // Apariencia
    private String theme;
    private String language;

    // Notificaciones en app
    private boolean notifyNewFollower;
    private boolean notifyNewReview;
    private boolean notifyNewFavorite;
    private boolean notifyEvents;
    private boolean notifyEmail;

    // Push (móvil)
    private boolean pushNewFollower; // siempre true
    private boolean pushNewReview;
    private boolean pushNewFavorite;
    private boolean pushEvents;
    private boolean pushSystem;

    // Cocina
    private String measurementSystem;

    // Privacidad
    private boolean publicProfile;
    private boolean showActivity;
}