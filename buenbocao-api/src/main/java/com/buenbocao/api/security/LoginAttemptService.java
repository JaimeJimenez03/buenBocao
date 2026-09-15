package com.buenbocao.api.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Controla los intentos fallidos de login por username.
 * Tras 5 fallos consecutivos, bloquea la cuenta durante 15 minutos.
 * El bloqueo se libera automáticamente al expirar, o al hacer login con éxito.
 */
@Slf4j
@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION_MS = 15 * 60 * 1000L;

    private record AttemptRecord(int count, Instant lockedUntil) {}

    private final ConcurrentHashMap<String, AttemptRecord> attempts = new ConcurrentHashMap<>();

    public void loginSucceeded(String username) {
        attempts.remove(username.toLowerCase());
    }

    public void loginFailed(String username) {
        String key = username.toLowerCase();
        attempts.compute(key, (k, existing) -> {
            if (existing == null || isExpired(existing)) {
                return new AttemptRecord(1, null);
            }
            int newCount = existing.count() + 1;
            Instant lockUntil = newCount >= MAX_ATTEMPTS
                    ? Instant.now().plusMillis(LOCKOUT_DURATION_MS)
                    : existing.lockedUntil();
            if (lockUntil != null && newCount == MAX_ATTEMPTS) {
                log.warn("[LOGIN_LOCKOUT] Usuario bloqueado por {} intentos fallidos: {}", newCount, k);
            }
            return new AttemptRecord(newCount, lockUntil);
        });
    }

    public boolean isBlocked(String username) {
        AttemptRecord record = attempts.get(username.toLowerCase());
        if (record == null) return false;
        if (record.lockedUntil() != null && Instant.now().isBefore(record.lockedUntil())) {
            return true;
        }
        if (isExpired(record)) {
            attempts.remove(username.toLowerCase());
        }
        return false;
    }

    public long getRemainingLockSeconds(String username) {
        AttemptRecord record = attempts.get(username.toLowerCase());
        if (record == null || record.lockedUntil() == null) return 0;
        long remaining = record.lockedUntil().getEpochSecond() - Instant.now().getEpochSecond();
        return Math.max(0, remaining);
    }

    private boolean isExpired(AttemptRecord record) {
        return record.lockedUntil() != null && Instant.now().isAfter(record.lockedUntil());
    }
}
