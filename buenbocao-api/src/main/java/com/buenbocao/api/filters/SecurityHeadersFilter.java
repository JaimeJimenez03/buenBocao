package com.buenbocao.api.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(1)
public class SecurityHeadersFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // Previene que el navegador deduzca el tipo de contenido (evita MIME sniffing attacks)
        response.setHeader("X-Content-Type-Options", "nosniff");

        // Impide que la app se embeba en iframes (protección contra clickjacking)
        response.setHeader("X-Frame-Options", "DENY");

        // Fuerza HTTPS durante 1 año, incluye subdominios
        response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");

        // Controla la información enviada en el header Referer
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        // Restringe acceso a APIs sensibles del navegador
        response.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=()");

        // Evita que respuestas de API se cacheen (tokens, datos privados)
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");

        // Content Security Policy: permite solo recursos propios
        // 'unsafe-inline' en script/style necesario para Swagger UI y panel admin con Thymeleaf
        response.setHeader("Content-Security-Policy",
                "default-src 'self'; " +
                "img-src 'self' data: blob:; " +
                "script-src 'self' 'unsafe-inline'; " +
                "style-src 'self' 'unsafe-inline'; " +
                "font-src 'self' data:; " +
                "connect-src 'self'; " +
                "frame-ancestors 'none'");

        filterChain.doFilter(request, response);
    }
}
