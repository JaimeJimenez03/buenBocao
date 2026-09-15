package com.buenbocao.api.stripe.controller;

import com.buenbocao.api.stripe.entity.StripeConfig;
import com.buenbocao.api.subscription.service.SubscriptionService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.Invoice;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/webhooks/stripe")
@RequiredArgsConstructor
public class StripeWebhookController {

    private final StripeConfig stripeConfig;
    private final SubscriptionService subscriptionService;

    /**
     * Endpoint que Stripe llama para notificar eventos de suscripción.
     *
     * Eventos gestionados:
     * - invoice.payment_succeeded → renovación automática completada
     * - invoice.payment_failed → fallo de cobro, notificar al usuario
     *
     * IMPORTANTE: este endpoint debe ser público (sin JWT).
     * Está en PUBLIC_ENDPOINTS de SecurityConfig: /webhooks/stripe
     */
    @PostMapping
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        // Verificar firma con Webhook.constructEvent — requiere WEBHOOK secret en .env
        // Para desarrollo local: stripe listen --forward-to
        // localhost:8080/api/v1/webhooks/stripe
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, stripeConfig.getWebhookSecret());
        } catch (SignatureVerificationException e) {
            log.warn("Webhook con firma invalida: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Firma invalida");
        } catch (Exception e) {
            log.error("Error parseando evento Stripe: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Payload invalido");
        }

        log.info("Webhook Stripe recibido: {}", event.getType());

        switch (event.getType()) {

            case "invoice.payment_succeeded" -> {
                event.getDataObjectDeserializer().getObject().ifPresent(obj -> {
                    Invoice invoice = (Invoice) obj;

                    // Solo procesar renovaciones (no el primer pago, que ya lo gestiona
                    // confirmSubscription)
                    if ("subscription_cycle".equals(invoice.getBillingReason())
                            || "subscription_update".equals(invoice.getBillingReason())) {

                        String subscriptionId = invoice.getSubscription();
                        String invoiceId = invoice.getId();

                        try {
                            com.stripe.model.Subscription stripeSub = com.stripe.model.Subscription
                                    .retrieve(subscriptionId);
                            Long periodEnd = stripeSub.getCurrentPeriodEnd();
                            subscriptionService.renewFromWebhook(subscriptionId, invoiceId, periodEnd);
                        } catch (Exception e) {
                            log.error("Error procesando renovacion webhook {}: {}",
                                    subscriptionId, e.getMessage());
                        }
                    }
                });
            }

            case "invoice.payment_failed" -> {
                event.getDataObjectDeserializer().getObject().ifPresent(obj -> {
                    Invoice invoice = (Invoice) obj;
                    String subscriptionId = invoice.getSubscription();
                    if (subscriptionId != null) {
                        subscriptionService.handlePaymentFailed(subscriptionId);
                    }
                });
            }

            case "customer.subscription.deleted" -> {
                // La suscripción fue cancelada definitivamente en Stripe (por impago o manual)
                event.getDataObjectDeserializer().getObject().ifPresent(obj -> {
                    com.stripe.model.Subscription stripeSub = (com.stripe.model.Subscription) obj;
                    log.info("Subscription {} eliminada en Stripe — marcando como EXPIRED", stripeSub.getId());
                    subscriptionService.cancelFromWebhook(stripeSub.getId());
                });
            }

            default -> log.debug("Evento Stripe no gestionado: {}", event.getType());
        }

        return ResponseEntity.ok("OK");
    }
}