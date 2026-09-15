package com.buenbocao.api.stripe.entity;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class StripeConfig {

    @Value("${stripe.secret-key}")
    private String secretKey;

    @Value("${stripe.publishable-key}")
    private String publishableKey;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    @Value("${stripe.currency}")
    private String currency;

    @Value("${stripe.statement-descriptor:Buen Bocao Premium}")
    private String statementDescriptor;

    @Value("${stripe.price-id.monthly:}")
    private String priceIdMonthly;

    @Value("${stripe.price-id.annual:}")
    private String priceIdAnnual;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }
}