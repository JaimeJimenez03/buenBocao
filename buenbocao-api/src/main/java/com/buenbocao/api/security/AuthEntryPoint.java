package com.buenbocao.api.security;

import com.buenbocao.api.common.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Punto de entrada para errores de autenticación.
 * - Peticiones del panel admin → redirige a /adminControlPanel/login
 * - Resto de peticiones (API) → devuelve JSON 401
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {

        if (request.getRequestURI().contains("/adminControlPanel")) {
            String contextPath = request.getContextPath();
            response.sendRedirect(contextPath + "/adminControlPanel/login");
            return;
        }

        // Para el resto de la API devolver JSON 401 como siempre
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ApiResponse<Void> apiResponse = ApiResponse.error("No estás autenticado. Inicia sesión para continuar.");
        objectMapper.writeValue(response.getOutputStream(), apiResponse);
    }
}