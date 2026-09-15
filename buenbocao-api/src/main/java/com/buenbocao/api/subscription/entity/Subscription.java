package com.buenbocao.api.subscription.entity;

import com.buenbocao.api.common.audit.AuditableEntity;
import com.buenbocao.api.common.enums.SubscriptionPlan;
import com.buenbocao.api.common.enums.SubscriptionStatus;
import com.buenbocao.api.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Subscription extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private SubscriptionPlan plan;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private SubscriptionStatus status = SubscriptionStatus.PENDING_PAYMENT;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal price;

    @Column(name = "starts_at")
    private LocalDateTime startsAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "payment_id", length = 200)
    private String paymentId;

    @Column(name = "stripe_customer_id", length = 200)
    private String stripeCustomerId;

    @Column(name = "stripe_subscription_id", length = 200)
    private String stripeSubscriptionId;

    @Column(name = "auto_renew", nullable = false)
    @Builder.Default
    private boolean autoRenew = true;

    public boolean isActive() {
        return status == SubscriptionStatus.ACTIVE
                && expiresAt != null
                && expiresAt.isAfter(LocalDateTime.now());
    }
}