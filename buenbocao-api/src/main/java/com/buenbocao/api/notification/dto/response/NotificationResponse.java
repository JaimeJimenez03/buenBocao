package com.buenbocao.api.notification.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private String type;
    private String title;
    private String message;
    private Long referenceId;
    private String referenceType;
    private String actorUsername;
    private String actorImage;
    private boolean read;
    private LocalDateTime createdAt;
}
