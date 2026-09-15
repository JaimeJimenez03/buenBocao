package com.buenbocao.api.subscription.controller;

import com.buenbocao.api.common.enums.SubscriptionPlan;
import com.buenbocao.api.common.response.ApiResponse;
import com.buenbocao.api.security.CustomUserDetails;
import com.buenbocao.api.stripe.dto.response.PaymentIntentResponse;
import com.buenbocao.api.subscription.dto.request.CreateSubscriptionRequest;
import com.buenbocao.api.subscription.dto.response.SubscriptionResponse;
import com.buenbocao.api.subscription.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Suscripciones", description = "Sistema de suscripcion premium con Stripe")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    /**
     * Paso 1: crear SetupIntent para guardar tarjeta e iniciar suscripcion
     * recurrente.
     * El frontend usa el clientSecret con initPaymentSheet de Stripe SDK.
     * Mantiene el path /create-payment-intent para compatibilidad con el frontend.
     */
    @Operation(summary = "Crear SetupIntent para suscripcion recurrente")
    @PostMapping("/create-payment-intent")
    public ResponseEntity<ApiResponse<PaymentIntentResponse>> createPaymentIntent(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam SubscriptionPlan plan) {
        com.buenbocao.api.stripe.dto.response.PaymentIntentResponse response = subscriptionService
                .createSetupIntent(currentUser, plan);
        return ResponseEntity.ok(ApiResponse.ok("SetupIntent creado", response));
    }

    /**
     * Paso 2: confirmar suscripcion tras pago exitoso.
     * La app llama a este endpoint despues de que Stripe confirme el pago,
     * pasando el paymentIntentId para verificacion en el backend.
     */
    @Operation(summary = "Confirmar suscripcion tras pago exitoso con Stripe")
    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> confirmSubscription(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody CreateSubscriptionRequest request) {
        SubscriptionResponse sub = subscriptionService.confirmSubscription(currentUser, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Suscripcion activada", sub));
    }

    @Operation(summary = "Ver mi suscripcion actual")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getMySubscription(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(ApiResponse.ok(subscriptionService.getMySubscription(currentUser.getId())));
    }

    @Operation(summary = "Cancelar suscripcion")
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> cancelSubscription(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(ApiResponse.ok("Suscripcion cancelada",
                subscriptionService.cancelSubscription(currentUser.getId())));
    }

    @Operation(summary = "Verificar si soy premium")
    @GetMapping("/me/is-premium")
    public ResponseEntity<ApiResponse<Boolean>> isPremium(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(ApiResponse.ok(subscriptionService.isPremium(currentUser.getId())));
    }
}