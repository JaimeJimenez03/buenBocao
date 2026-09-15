package com.buenbocao.api.filters;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Filtro de version y mantenimiento.
 * Solo aplica restricciones a clientes mobile que envian X-App-Version.
 * Navegadores, Swagger y Expo Web pasan libremente.
 */
@Component
@Order(1)
public class AppVersionFilter implements Filter {

    @Value("${app.min-version:1.0.0}")
    private String minVersion;

    @Value("${app.maintenance:false}")
    private boolean maintenance;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(req, res);
            return;
        }

        String path = request.getRequestURI();
        if (isExemptPath(path)) {
            chain.doFilter(req, res);
            return;
        }

        String appVersion = request.getHeader("X-App-Version");
        String platform = request.getHeader("X-App-Platform");

        if (appVersion == null || platform == null) {
            chain.doFilter(req, res);
            return;
        }

        if (maintenance) {
            response.setStatus(503);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Aplicacion en mantenimiento\"}");
            return;
        }

        if (isVersionLower(appVersion, minVersion)) {
            response.setStatus(426);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Actualiza la app para continuar\"}");
            return;
        }

        chain.doFilter(req, res);
    }

    private boolean isExemptPath(String path) {
        return path.contains("/auth/")
                || path.contains("/swagger")
                || path.contains("/api-docs")
                || path.contains("/storage/")
                || path.contains("/api/version")
                || path.contains("/error");
    }

    private boolean isVersionLower(String current, String min) {
        if (!isValidVersion(current) || !isValidVersion(min))
            return false;
        String[] c = current.split("\\.");
        String[] m = min.split("\\.");
        for (int i = 0; i < Math.max(c.length, m.length); i++) {
            int cv = i < c.length ? Integer.parseInt(c[i]) : 0;
            int mv = i < m.length ? Integer.parseInt(m[i]) : 0;
            if (cv < mv)
                return true;
            if (cv > mv)
                return false;
        }
        return false;
    }

    private boolean isValidVersion(String version) {
        return version != null && version.matches("\\d+(\\.\\d+)*");
    }
}
