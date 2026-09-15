package com.buenbocao.api.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate limiting en tres capas:
 *
 *  1. Global por IP        — 300 req/min en cualquier endpoint de la API
 *  2. Escritura por IP     — 40 POST/PUT/PATCH/DELETE por minuto
 *  3. Auth sensible por IP — 5 intentos cada 15 min en /auth/login y /auth/register
 *  4. Auth general por IP  — 20 req/min en /auth/**
 *
 * Los mapas se limpian automáticamente cada 5 minutos para evitar
 * acumulación de entradas expiradas en memoria.
 */
@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    // ── Límites ───────────────────────────────────────────────────────────────

    private static final int  MAX_GLOBAL_PER_MIN   = 300;
    private static final long WINDOW_MIN_MS         = 60_000L;

    private static final int  MAX_WRITES_PER_MIN    = 40;

    private static final int  MAX_AUTH_PER_MIN      = 20;

    private static final int  MAX_LOGIN_PER_WINDOW  = 5;
    private static final long WINDOW_LOGIN_MS       = 15 * 60_000L;

    // ── Almacenes en memoria ──────────────────────────────────────────────────

    private final ConcurrentHashMap<String, RateRecord> globalStore  = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, RateRecord> writeStore   = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, RateRecord> authStore    = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, RateRecord> loginStore   = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;

    // ── Record interno ────────────────────────────────────────────────────────

    private record RateRecord(AtomicInteger count, long windowStartMs) {}

    // ── Filtro principal ──────────────────────────────────────────────────────

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String path   = request.getServletPath();
        String method = request.getMethod();
        String ip     = extractClientIp(request);

        // 1. Límite global (todas las rutas de la API excepto storage y webhooks)
        if (isApiPath(path)) {
            RateResult global = check(globalStore, ip + ":global", MAX_GLOBAL_PER_MIN, WINDOW_MIN_MS);
            addQuotaHeaders(response, MAX_GLOBAL_PER_MIN, global.remaining(), 60);
            if (global.blocked()) {
                log.warn("[RATE_LIMIT] Global IP bloqueada: {} → {}", ip, path);
                sendError(response, 429, "Demasiadas peticiones. Espera un momento antes de continuar.",
                        global.retryAfterSeconds());
                return;
            }

            // 2. Límite en operaciones de escritura
            if (isWriteMethod(method)) {
                RateResult write = check(writeStore, ip + ":write", MAX_WRITES_PER_MIN, WINDOW_MIN_MS);
                if (write.blocked()) {
                    log.warn("[RATE_LIMIT] Escritura bloqueada: {} {} → {}", method, path, ip);
                    sendError(response, 429, "Demasiadas operaciones de escritura. Espera un minuto.",
                            write.retryAfterSeconds());
                    return;
                }
            }
        }

        // 3 y 4. Límites específicos de autenticación
        if (path.startsWith("/auth/")) {
            if (path.equals("/auth/login") || path.equals("/auth/register")) {
                RateResult login = check(loginStore, ip + ":login", MAX_LOGIN_PER_WINDOW, WINDOW_LOGIN_MS);
                if (login.blocked()) {
                    log.warn("[RATE_LIMIT] Login/register bloqueado: {} → {}", ip, path);
                    sendError(response, 429, "Demasiados intentos. Espera 15 minutos antes de volver a intentarlo.",
                            login.retryAfterSeconds());
                    return;
                }
            }

            RateResult auth = check(authStore, ip + ":auth", MAX_AUTH_PER_MIN, WINDOW_MIN_MS);
            if (auth.blocked()) {
                log.warn("[RATE_LIMIT] Auth general bloqueado: {}", ip);
                sendError(response, 429, "Demasiadas solicitudes de autenticación. Espera un momento.",
                        auth.retryAfterSeconds());
                return;
            }
        }

        chain.doFilter(request, response);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean isApiPath(String path) {
        return !path.startsWith("/storage/")
                && !path.startsWith("/webhooks/")
                && !path.startsWith("/swagger")
                && !path.startsWith("/api-docs")
                && !path.startsWith("/v3/api-docs")
                && !path.equals("/error");
    }

    private boolean isWriteMethod(String method) {
        return "POST".equalsIgnoreCase(method)
                || "PUT".equalsIgnoreCase(method)
                || "PATCH".equalsIgnoreCase(method)
                || "DELETE".equalsIgnoreCase(method);
    }

    private RateResult check(ConcurrentHashMap<String, RateRecord> store,
                              String key, int maxRequests, long windowMs) {
        long now = System.currentTimeMillis();
        RateRecord record = store.compute(key, (k, existing) -> {
            if (existing == null || now - existing.windowStartMs() > windowMs) {
                return new RateRecord(new AtomicInteger(1), now);
            }
            existing.count().incrementAndGet();
            return existing;
        });

        int current   = record.count().get();
        int remaining = Math.max(0, maxRequests - current);
        boolean blocked = current > maxRequests;

        long retryAfterMs = windowMs - (now - record.windowStartMs());
        int  retryAfterSec = (int) Math.max(1, retryAfterMs / 1000);

        return new RateResult(blocked, remaining, retryAfterSec);
    }

    private String extractClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
        String xri = request.getHeader("X-Real-IP");
        if (xri != null && !xri.isBlank()) return xri.trim();
        return request.getRemoteAddr();
    }

    private void addQuotaHeaders(HttpServletResponse response, int limit, int remaining, int resetSeconds) {
        response.setHeader("X-RateLimit-Limit",     String.valueOf(limit));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));
        response.setHeader("X-RateLimit-Reset",     String.valueOf(resetSeconds));
    }

    private void sendError(HttpServletResponse response, int status, String message, int retryAfter)
            throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Retry-After", String.valueOf(retryAfter));
        Map<String, Object> body = Map.of(
                "success", false,
                "message", message,
                "status",  status
        );
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    // ── Limpieza periódica (cada 5 min) ──────────────────────────────────────
    // Evita que las entradas expiradas acumulen memoria indefinidamente.

    @Scheduled(fixedDelay = 300_000L)
    public void evictExpiredEntries() {
        long now = System.currentTimeMillis();
        evict(globalStore, WINDOW_MIN_MS);
        evict(writeStore,  WINDOW_MIN_MS);
        evict(authStore,   WINDOW_MIN_MS);
        evict(loginStore,  WINDOW_LOGIN_MS);
        log.debug("[RATE_LIMIT] Limpieza de entradas expiradas completada");
    }

    private void evict(ConcurrentHashMap<String, RateRecord> store, long windowMs) {
        long now = System.currentTimeMillis();
        store.entrySet().removeIf(e -> now - e.getValue().windowStartMs() > windowMs);
    }

    // ── Record de resultado ───────────────────────────────────────────────────

    private record RateResult(boolean blocked, int remaining, int retryAfterSeconds) {}
}
