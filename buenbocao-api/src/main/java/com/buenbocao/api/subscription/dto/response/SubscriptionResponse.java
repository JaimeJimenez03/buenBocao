package com.buenbocao.api.subscription.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class SubscriptionResponse {
    private Long id;
    private String plan;
    private String status;
    private BigDecimal price;
    private LocalDateTime startsAt;
    private LocalDateTime expiresAt;
    private boolean autoRenew;
    private boolean active;
    private long daysRemaining;
}
