package com.buenbocao.api.moderation.dto.request;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class BanRequest {
    private String reason;
    private Integer durationDays;
}
