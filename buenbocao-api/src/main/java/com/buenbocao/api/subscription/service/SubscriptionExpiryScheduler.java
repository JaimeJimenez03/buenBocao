package com.buenbocao.api.subscription.service;

import com.buenbocao.api.common.enums.SubscriptionStatus;
import com.buenbocao.api.notification.service.NotificationService;
import com.buenbocao.api.subscription.entity.Subscription;
import com.buenbocao.api.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Comprueba cada minuto las suscripciones activas que han caducado
 * y las marca como EXPIRED notificando al usuario.
 *
 * ⚠ SOLO PARA PRUEBAS: cron cada minuto.
 * En produccion cambiar a: "0 0 2 * * *" (cada dia a las 2:00 AM)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionExpiryScheduler {

    private final SubscriptionRepository subscriptionRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void expireSubscriptions() {
        LocalDateTime now = LocalDateTime.now();

        List<Subscription> expired = subscriptionRepository
                .findByStatusAndExpiresAtBefore(SubscriptionStatus.ACTIVE, now);

        for (Subscription sub : expired) {
            sub.setStatus(SubscriptionStatus.EXPIRED);
            subscriptionRepository.save(sub);

            notificationService.notifySystem(
                    sub.getUser().getId(),
                    "Tu suscripción Premium ha caducado",
                    "Tu suscripción " +
                            (sub.getPlan().name().contains("MONTHLY") ? "mensual" : "anual") +
                            " ha caducado. Renuévala para seguir disfrutando de todos los beneficios premium.");

            log.info("Suscripcion #{} del usuario {} marcada como EXPIRED (caducó el {})",
                    sub.getId(), sub.getUser().getUsername(), sub.getExpiresAt());
        }

        if (!expired.isEmpty()) {
            log.info("Scheduler de caducidad: {} suscripciones expiradas", expired.size());
        }

        // 2. Avisar a las que caducan en los próximos 3 días
        LocalDateTime in3Days = now.plusDays(3);
        List<Subscription> expiringSoon = subscriptionRepository
                .findByStatusAndExpiresAtBetween(SubscriptionStatus.ACTIVE, now, in3Days);

        for (Subscription sub : expiringSoon) {
            long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(now, sub.getExpiresAt());
            String cuando = daysLeft == 0 ? "hoy" : (daysLeft == 1 ? "mañana" : "en " + daysLeft + " días");

            notificationService.notifySystem(
                    sub.getUser().getId(),
                    "Tu suscripción caduca pronto",
                    "Tu suscripción Premium caduca " + cuando
                            + ". Renuévala para no perder el acceso a los beneficios premium.");

            log.info("Aviso de caducidad enviado al usuario {} — caduca {}",
                    sub.getUser().getUsername(), sub.getExpiresAt());
        }

        if (!expiringSoon.isEmpty()) {
            log.info("Scheduler de caducidad: {} avisos de proxima caducidad enviados", expiringSoon.size());
        }
    }
}