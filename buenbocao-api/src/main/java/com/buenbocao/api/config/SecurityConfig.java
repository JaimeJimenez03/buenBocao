package com.buenbocao.api.config;

import com.buenbocao.api.security.AuthEntryPoint;
import com.buenbocao.api.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final AuthEntryPoint authEntryPoint;
        private final CorsConfigurationSource corsConfigurationSource;

        private static final String[] PUBLIC_ENDPOINTS = {
                        "/auth/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/api-docs/**",
                        "/v3/api-docs/**",
                        "/error",
                        "/version",
                        "/adminControlPanel/login",
                        "/webhooks/stripe"
        };

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                return http
                                .csrf(AbstractHttpConfigurer::disable)
                                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                                .exceptionHandling(exception -> exception
                                                .authenticationEntryPoint(authEntryPoint))
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                // ── Endpoints públicos ──────────────────────────────
                                                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                                                .requestMatchers(HttpMethod.GET, "/recipes/**").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/users/*/profile").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/users/*/followers").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/users/*/following").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/recipes/*/reviews").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/recipes/*/reviews/stats").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/feed/discover").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/events/**").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/storage/**").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/users/search").permitAll()
                                                // WebSocket — handshake SockJS
                                                .requestMatchers("/ws/**").permitAll()
                                                // ── Panel de administración ─────────────────────────
                                                .requestMatchers("/adminControlPanel/**").hasRole("ADMIN")
                                                // ── El resto requiere autenticación ─────────────────
                                                .anyRequest().authenticated())
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                                .build();
        }

        @Bean
        public AuthenticationManager authenticationManager(
                        AuthenticationConfiguration authenticationConfiguration) throws Exception {
                return authenticationConfiguration.getAuthenticationManager();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}