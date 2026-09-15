package com.buenbocao.api.event.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {
    private Long id;
    private String title;
    private String description;
    private String slug;
    private String type;
    private String status;
    private String coverImage;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private String location;
    private boolean online;
    private Integer maxParticipants;
    private long currentParticipants;
    private boolean premiumOnly;
    private boolean full;
    private boolean joinedByCurrentUser;
    private String organizerUsername;
    private String tags;
    private java.math.BigDecimal price;
    private boolean free;
    private String joinUrl;
    private Integer minParticipants;
    private LocalDateTime registrationDeadline;
    private String requirements;
    private LocalDateTime createdAt;
}