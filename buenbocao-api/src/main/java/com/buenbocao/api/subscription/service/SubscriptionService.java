package com.buenbocao.api.subscription.service;

import com.buenbocao.api.common.enums.SubscriptionPlan;
import com.buenbocao.api.common.enums.SubscriptionStatus;
import com.buenbocao.api.common.exception.BusinessException;
import com.buenbocao.api.common.exception.ResourceNotFoundException;
import com.buenbocao.api.notification.service.NotificationService;
import com.buenbocao.api.security.CustomUserDetails;
import com.buenbocao.api.stripe.dto.response.PaymentIntentResponse;
import com.buenbocao.api.stripe.entity.StripeConfig;
import com.buenbocao.api.subscription.dto.request.CreateSubscriptionRequest;
import com.buenbocao.api.subscription.dto.response.SubscriptionResponse;
import com.buenbocao.api.subscription.entity.Subscription;
import com.buenbocao.api.subscription.repository.SubscriptionRepository;
import com.buenbocao.api.user.entity.User;
import com.buenbocao.api.user.repository.UserRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.SetupIntent;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.SetupIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

        private final SubscriptionRepository subscriptionRepository;
        private final UserRepository userRepository;
        private final StripeConfig stripeConfig;
        private final NotificationService notificationService;

        private static final BigDecimal MONTHLY_PRICE = new BigDecimal("2.99");
        private static final BigDecimal ANNUAL_PRICE = new BigDecimal("24.99");

        // ─── Paso 1: Crear SetupIntent ─────────────────────────────────────────────
        //
        // En lugar de PaymentIntent (pago único), usamos SetupIntent para guardar
        // la tarjeta del usuario. Stripe luego cobra automáticamente cada mes/año
        // a través de la Subscription recurrente.

        @Transactional
        public PaymentIntentResponse createSetupIntent(CustomUserDetails currentUser, SubscriptionPlan plan) {
                if (plan == SubscriptionPlan.FREE) {
                        throw new BusinessException("No es necesario pago para el plan gratuito.");
                }
                if (subscriptionRepository.existsByUserIdAndStatus(currentUser.getId(), SubscriptionStatus.ACTIVE)) {
                        throw new BusinessException("Ya tienes una suscripcion activa.");
                }

                User user = userRepository.findById(currentUser.getId())
                                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", currentUser.getId()));

                String priceDisplay = plan == SubscriptionPlan.PREMIUM_MONTHLY ? "2,99€/mes" : "24,99€/año";

                try {
                        // Crear o reutilizar Customer de Stripe
                        String customerId = getOrCreateStripeCustomer(user);

                        // Crear SetupIntent para guardar la tarjeta
                        SetupIntentCreateParams siParams = SetupIntentCreateParams.builder()
                                        .setCustomer(customerId)
                                        .addPaymentMethodType("card")
                                        .putMetadata("userId", String.valueOf(user.getId()))
                                        .putMetadata("plan", plan.name())
                                        .putMetadata("stripeCustomerId", customerId)
                                        .build();

                        SetupIntent setupIntent = SetupIntent.create(siParams);
                        log.info("SetupIntent creado: {} para usuario {} plan {}", setupIntent.getId(),
                                        user.getUsername(), plan);

                        return PaymentIntentResponse.builder()
                                        .clientSecret(setupIntent.getClientSecret())
                                        .priceDisplay(priceDisplay)
                                        .plan(plan.name())
                                        .build();

                } catch (StripeException e) {
                        log.error("Error creando SetupIntent: {}", e.getMessage());
                        throw new BusinessException("Error al iniciar el pago. Intentalo de nuevo.");
                }
        }

        // ─── Paso 2: Confirmar — crear Subscription en Stripe ─────────────────────

        @Transactional
        public SubscriptionResponse confirmSubscription(CustomUserDetails currentUser,
                        CreateSubscriptionRequest request) {
                if (subscriptionRepository.existsByUserIdAndStatus(currentUser.getId(), SubscriptionStatus.ACTIVE)) {
                        throw new BusinessException("Ya tienes una suscripcion activa.");
                }

                User user = userRepository.findById(currentUser.getId())
                                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", currentUser.getId()));

                try {
                        // Recuperar el SetupIntent para obtener el paymentMethod guardado
                        SetupIntent setupIntent = SetupIntent.retrieve(request.getPaymentId());

                        if (!"succeeded".equals(setupIntent.getStatus())) {
                                throw new BusinessException(
                                                "La tarjeta no se pudo verificar. Estado: " + setupIntent.getStatus());
                        }

                        String customerId = setupIntent.getCustomer();
                        String paymentMethodId = setupIntent.getPaymentMethod();
                        String planStr = setupIntent.getMetadata().get("plan");

                        if (!request.getPlan().name().equals(planStr)) {
                                throw new BusinessException("El plan no coincide con el configurado en el pago.");
                        }

                        // Asignar el paymentMethod como predeterminado del Customer
                        com.stripe.model.Customer customer = com.stripe.model.Customer.retrieve(customerId);
                        customer.update(com.stripe.param.CustomerUpdateParams.builder()
                                        .setInvoiceSettings(
                                                        com.stripe.param.CustomerUpdateParams.InvoiceSettings.builder()
                                                                        .setDefaultPaymentMethod(paymentMethodId)
                                                                        .build())
                                        .build());

                        // Crear la Subscription recurrente en Stripe
                        String priceId = request.getPlan() == SubscriptionPlan.PREMIUM_MONTHLY
                                        ? stripeConfig.getPriceIdMonthly()
                                        : stripeConfig.getPriceIdAnnual();

                        if (priceId == null || priceId.isBlank()) {
                                throw new BusinessException(
                                                "Price ID de Stripe no configurado. Contacta con el administrador.");
                        }

                        com.stripe.param.SubscriptionCreateParams subParams = com.stripe.param.SubscriptionCreateParams
                                        .builder()
                                        .setCustomer(customerId)
                                        .addItem(com.stripe.param.SubscriptionCreateParams.Item.builder()
                                                        .setPrice(priceId)
                                                        .build())
                                        .setDefaultPaymentMethod(paymentMethodId)
                                        .putMetadata("userId", String.valueOf(user.getId()))
                                        .putMetadata("plan", request.getPlan().name())
                                        .build();

                        com.stripe.model.Subscription stripeSub = com.stripe.model.Subscription.create(subParams);

                        log.info("Stripe Subscription creada: {} para usuario {} plan {}",
                                        stripeSub.getId(), user.getUsername(), request.getPlan());

                        // Calcular fecha de expiración desde Stripe
                        LocalDateTime expiresAt = LocalDateTime.ofEpochSecond(
                                        stripeSub.getCurrentPeriodEnd(), 0,
                                        java.time.ZoneOffset.UTC);

                        BigDecimal price = request.getPlan() == SubscriptionPlan.PREMIUM_MONTHLY
                                        ? MONTHLY_PRICE
                                        : ANNUAL_PRICE;

                        Subscription subscription = Subscription.builder()
                                        .user(user)
                                        .plan(request.getPlan())
                                        .status(SubscriptionStatus.ACTIVE)
                                        .price(price)
                                        .startsAt(LocalDateTime.now())
                                        .expiresAt(expiresAt)
                                        .paymentId(stripeSub.getLatestInvoice())
                                        .stripeCustomerId(customerId)
                                        .stripeSubscriptionId(stripeSub.getId())
                                        .autoRenew(true)
                                        .build();

                        Subscription saved = subscriptionRepository.save(subscription);
                        log.info("Suscripcion activada para {} — caduca {}", user.getUsername(), expiresAt);

                        notificationService.notifySystem(user.getId(), "Suscripcion Premium activada",
                                        "Tu suscripcion " + (request.getPlan() == SubscriptionPlan.PREMIUM_MONTHLY
                                                        ? "mensual (2,99€/mes)"
                                                        : "anual (24,99€/año)")
                                                        + " ha sido activada. Se renovara automaticamente. Disfruta de todos los beneficios.");

                        return toResponse(saved);

                } catch (StripeException e) {
                        log.error("Error creando Stripe Subscription: {}", e.getMessage());
                        throw new BusinessException("Error al activar la suscripcion. Contacta con soporte.");
                }
        }

        // ─── Renovación automática desde webhook ──────────────────────────────────

        /**
         * Llamado por el webhook cuando Stripe renueva la suscripción
         * (evento invoice.payment_succeeded con subscription_cycle).
         */
        @Transactional
        public void renewFromWebhook(String stripeSubscriptionId, String invoiceId,
                        Long periodEnd) {
                subscriptionRepository.findByStripeSubscriptionId(stripeSubscriptionId)
                                .ifPresentOrElse(sub -> {
                                        LocalDateTime newExpiry = LocalDateTime.ofEpochSecond(
                                                        periodEnd, 0, java.time.ZoneOffset.UTC);

                                        sub.setStatus(SubscriptionStatus.ACTIVE);
                                        sub.setExpiresAt(newExpiry);
                                        sub.setPaymentId(invoiceId);
                                        subscriptionRepository.save(sub);

                                        notificationService.notifySystem(sub.getUser().getId(),
                                                        "Suscripcion renovada automaticamente",
                                                        "Tu suscripcion premium se ha renovado. Proximo cobro: "
                                                                        + newExpiry.toLocalDate().toString() + ".");

                                        log.info("Suscripcion {} renovada hasta {} (webhook)",
                                                        stripeSubscriptionId, newExpiry);
                                }, () -> log.warn("Webhook renovacion: subscription {} no encontrada en BD",
                                                stripeSubscriptionId));
        }

        // ─── Fallo de pago desde webhook ──────────────────────────────────────────

        @Transactional
        public void handlePaymentFailed(String stripeSubscriptionId) {
                subscriptionRepository.findByStripeSubscriptionId(stripeSubscriptionId)
                                .ifPresent(sub -> {
                                        notificationService.notifySystem(sub.getUser().getId(),
                                                        "Pago fallido — suscripcion en riesgo",
                                                        "No hemos podido cobrar tu suscripcion premium. "
                                                                        + "Stripe lo reintentara automaticamente. "
                                                                        + "Si el problema persiste, actualiza tu metodo de pago.");

                                        log.warn("Pago fallido para subscription {} usuario {}",
                                                        stripeSubscriptionId, sub.getUser().getUsername());
                                });
        }

        // ─── Cancelación definitiva desde webhook ─────────────────────────────────

        /**
         * Llamado por el webhook customer.subscription.deleted.
         * Stripe ha cancelado la suscripción definitivamente (por impago o manual).
         * Marcamos como EXPIRED en BD para que el usuario pierda acceso.
         */
        @Transactional
        public void cancelFromWebhook(String stripeSubscriptionId) {
                subscriptionRepository.findByStripeSubscriptionId(stripeSubscriptionId)
                                .ifPresent(sub -> {
                                        sub.setStatus(SubscriptionStatus.EXPIRED);
                                        sub.setAutoRenew(false);
                                        subscriptionRepository.save(sub);

                                        notificationService.notifySystem(sub.getUser().getId(),
                                                        "Suscripcion cancelada",
                                                        "Tu suscripcion Premium ha sido cancelada y has perdido el acceso. "
                                                                        + "Puedes volver a suscribirte en cualquier momento.");

                                        log.info("Subscription {} cancelada definitivamente por Stripe para usuario {}",
                                                        stripeSubscriptionId, sub.getUser().getUsername());
                                });
        }

        // ─── Cancelar ─────────────────────────────────────────────────────────────

        @Transactional
        public SubscriptionResponse cancelSubscription(Long userId) {
                Subscription subscription = subscriptionRepository
                                .findFirstByUserIdAndStatusOrderByExpiresAtDesc(userId, SubscriptionStatus.ACTIVE)
                                .orElseThrow(() -> new BusinessException("No tienes una suscripcion activa."));

                // Cancelar en Stripe al final del periodo (no inmediatamente)
                if (subscription.getStripeSubscriptionId() != null) {
                        try {
                                com.stripe.model.Subscription stripeSub = com.stripe.model.Subscription
                                                .retrieve(subscription.getStripeSubscriptionId());
                                stripeSub.update(com.stripe.param.SubscriptionUpdateParams.builder()
                                                .setCancelAtPeriodEnd(true)
                                                .build());
                                log.info("Stripe Subscription {} marcada para cancelar al fin de periodo",
                                                subscription.getStripeSubscriptionId());
                        } catch (StripeException e) {
                                log.error("Error cancelando en Stripe: {}", e.getMessage());
                        }
                }

                // No cambiar el status a CANCELLED inmediatamente:
                // el usuario conserva el acceso hasta expiresAt.
                // El scheduler marcará la suscripción como EXPIRED cuando caduque.
                subscription.setAutoRenew(false);
                Subscription updated = subscriptionRepository.save(subscription);

                notificationService.notifySystem(userId, "Renovación automática desactivada",
                                "Has cancelado la renovación de tu suscripción Premium. "
                                                + "Mantendrás todos los beneficios hasta el "
                                                + (updated.getExpiresAt() != null
                                                                ? updated.getExpiresAt().toLocalDate().toString()
                                                                : "fin del periodo actual")
                                                + ".");

                return toResponse(updated);
        }

        // ─── Consultas ─────────────────────────────────────────────────────────────

        @Transactional(readOnly = true)
        public SubscriptionResponse getMySubscription(Long userId) {
                return subscriptionRepository.findFirstByUserIdOrderByCreatedAtDesc(userId)
                                .map(this::toResponse)
                                .orElseGet(() -> SubscriptionResponse.builder()
                                                .plan(SubscriptionPlan.FREE.name())
                                                .status("NONE")
                                                .active(false)
                                                .daysRemaining(0)
                                                .build());
        }

        @Transactional(readOnly = true)
        public boolean isPremium(Long userId) {
                return subscriptionRepository
                                .findFirstByUserIdAndStatusOrderByExpiresAtDesc(userId, SubscriptionStatus.ACTIVE)
                                .map(Subscription::isActive)
                                .orElse(false);
        }

        // ─── Helpers privados ──────────────────────────────────────────────────────

        private String getOrCreateStripeCustomer(User user) throws StripeException {
                // Buscar si ya tiene un customer en alguna suscripcion previa
                return subscriptionRepository.findFirstByUserIdOrderByCreatedAtDesc(user.getId())
                                .map(Subscription::getStripeCustomerId)
                                .filter(id -> id != null && !id.isBlank())
                                .orElseGet(() -> {
                                        try {
                                                Customer customer = Customer.create(
                                                                CustomerCreateParams.builder()
                                                                                .setEmail(user.getEmail())
                                                                                .setName(user.getFirstName() + " "
                                                                                                + user.getLastName())
                                                                                .putMetadata("userId", String
                                                                                                .valueOf(user.getId()))
                                                                                .putMetadata("username",
                                                                                                user.getUsername())
                                                                                .build());
                                                log.info("Customer de Stripe creado: {} para {}", customer.getId(),
                                                                user.getUsername());
                                                return customer.getId();
                                        } catch (StripeException e) {
                                                throw new RuntimeException(
                                                                "Error creando Customer en Stripe: " + e.getMessage(),
                                                                e);
                                        }
                                });
        }

        private SubscriptionResponse toResponse(Subscription s) {
                long daysRemaining = 0;
                if (s.getExpiresAt() != null && s.getExpiresAt().isAfter(LocalDateTime.now())) {
                        daysRemaining = ChronoUnit.DAYS.between(LocalDateTime.now(), s.getExpiresAt());
                }
                return SubscriptionResponse.builder()
                                .id(s.getId())
                                .plan(s.getPlan().name())
                                .status(s.getStatus().name())
                                .price(s.getPrice())
                                .startsAt(s.getStartsAt())
                                .expiresAt(s.getExpiresAt())
                                .autoRenew(s.isAutoRenew())
                                .active(s.isActive())
                                .daysRemaining(daysRemaining)
                                .build();
        }
}