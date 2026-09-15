package com.buenbocao.api.security.jwt;

import com.buenbocao.api.security.CustomUserDetails;
import com.buenbocao.api.user.entity.User;
import com.buenbocao.api.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        log.debug("FILTER HIT: {} {} | Auth: {}",
                request.getMethod(),
                request.getRequestURI(),
                request.getHeader("Authorization") != null ? "[present]" : "[absent]");

        String token = extractTokenFromRequest(request);

        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            String username = jwtTokenProvider.getUsernameFromToken(token);

            // Comprobar ban en cada petición
            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null && !user.isEnabled()) {
                // Si el ban ha expirado, levantarlo automáticamente
                if (user.getBanExpiresAt() != null && user.getBanExpiresAt().isBefore(LocalDateTime.now())) {
                    user.setEnabled(true);
                    user.setBanReason(null);
                    user.setBanExpiresAt(null);
                    userRepository.save(user);
                    log.info("[BAN] Ban expirado para usuario {}, cuenta reactivada", username);
                } else {
                    // Ban activo — rechazar la petición con 403
                    String msg = "Tu cuenta está suspendida.";
                    if (user.getBanReason() != null) {
                        msg += " Motivo: " + user.getBanReason() + ".";
                    }
                    if (user.getBanExpiresAt() != null) {
                        long daysLeft = ChronoUnit.DAYS.between(LocalDateTime.now(), user.getBanExpiresAt());
                        msg += " Tiempo restante: " + (daysLeft <= 0 ? "menos de 1 día" : daysLeft + " días") + ".";
                    } else {
                        msg += " Este ban es permanente.";
                    }
                    log.warn("[BAN] Petición bloqueada para usuario baneado: {}", username);
                    sendBanError(response, msg);
                    return; // No continuar con el filtro
                }
            }

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private void sendBanError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        Map<String, Object> body = Map.of(
                "success", false,
                "message", message,
                "status", 403
        );
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
