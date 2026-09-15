package com.buenbocao.api.subscription.repository;

import com.buenbocao.api.common.enums.SubscriptionStatus;
import com.buenbocao.api.subscription.entity.Subscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findFirstByUserIdAndStatusOrderByExpiresAtDesc(Long userId, SubscriptionStatus status);

    Optional<Subscription> findFirstByUserIdOrderByCreatedAtDesc(Long userId);

    List<Subscription> findByStatusAndExpiresAtBefore(SubscriptionStatus status, LocalDateTime now);

    List<Subscription> findByStatusAndExpiresAtBetween(
            SubscriptionStatus status, LocalDateTime from, LocalDateTime to);

    boolean existsByUserIdAndStatus(Long userId, SubscriptionStatus status);

    // ── Añadidos para el panel admin ──
    Page<Subscription> findByStatus(SubscriptionStatus status, Pageable pageable);

    long countByStatus(SubscriptionStatus status);

    List<Subscription> findByUserIdOrderByCreatedAtDesc(Long userId);

    java.util.Optional<Subscription> findByStripeSubscriptionId(String stripeSubscriptionId);
}