package com.buenbocao.api.subscription.dto.request;

import com.buenbocao.api.common.enums.SubscriptionPlan;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CreateSubscriptionRequest {

    @NotNull(message = "El plan es obligatorio")
    private SubscriptionPlan plan;

    /** ID de pago del proveedor (provisional - se validará con Stripe/etc.). */
    private String paymentId;
}
