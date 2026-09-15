package com.buenbocao.api.auth.service;

import com.buenbocao.api.auth.dto.request.ForgotPasswordRequest;
import com.buenbocao.api.auth.dto.request.LoginRequest;
import com.buenbocao.api.auth.dto.request.RefreshTokenRequest;
import com.buenbocao.api.auth.dto.request.RegisterRequest;
import com.buenbocao.api.auth.dto.request.ResetPasswordRequest;
import com.buenbocao.api.auth.dto.response.AuthResponse;
import com.buenbocao.api.common.exception.BusinessException;
import com.buenbocao.api.common.exception.DuplicateResourceException;
import com.buenbocao.api.common.exception.ResourceNotFoundException;
import com.buenbocao.api.email.service.EmailService;
import com.buenbocao.api.security.LoginAttemptService;
import com.buenbocao.api.security.jwt.JwtTokenProvider;
import com.buenbocao.api.user.entity.User;
import com.buenbocao.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Servicio de autenticación.
 * Maneja registro, login, refresh de tokens y verificación de email.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;
    private final LoginAttemptService loginAttemptService;

    /**
     * Registra un nuevo usuario en la plataforma.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Validar que el username y email no estén en uso
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Usuario", "username", request.getUsername());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Usuario", "email", request.getEmail());
        }

        // Crear el usuario
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .emailVerificationToken(UUID.randomUUID().toString())
                .build();

        User savedUser = userRepository.save(user);
        log.info("Nuevo usuario registrado: {}", savedUser.getUsername());

        // Enviar email de verificación (asíncrono)
        emailService.sendVerificationEmail(
                savedUser.getEmail(),
                savedUser.getUsername(),
                savedUser.getEmailVerificationToken());

        // Generar tokens y devolver respuesta
        String accessToken = jwtTokenProvider.generateAccessToken(savedUser.getUsername());
        String refreshToken = jwtTokenProvider.generateRefreshToken(savedUser.getUsername());

        return buildAuthResponse(savedUser, accessToken, refreshToken);
    }

    /**
     * Inicia sesión con username y contraseña.
     */
    public AuthResponse login(LoginRequest request) {
        String username = request.getUsername();

        // Comprobar bloqueo por intentos fallidos
        if (loginAttemptService.isBlocked(username)) {
            long seconds = loginAttemptService.getRemainingLockSeconds(username);
            long minutes = Math.max(1, (seconds + 59) / 60);
            throw new BusinessException(
                    "Cuenta bloqueada temporalmente por múltiples intentos fallidos. " +
                    "Inténtalo de nuevo en " + minutes + " minuto(s).");
        }

        // Comprobar ban antes de autenticar (para dar mensaje claro)
        userRepository.findByUsername(username).ifPresent(u -> {
            if (!u.isEnabled()) {
                if (u.getBanExpiresAt() != null && u.getBanExpiresAt().isBefore(LocalDateTime.now())) {
                    u.setEnabled(true);
                    u.setBanReason(null);
                    u.setBanExpiresAt(null);
                    userRepository.save(u);
                } else {
                    String msg = "Tu cuenta ha sido suspendida.";
                    if (u.getBanReason() != null) msg += " Motivo: " + u.getBanReason() + ".";
                    if (u.getBanExpiresAt() != null) {
                        long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), u.getBanExpiresAt());
                        msg += " Tiempo restante: " + (daysLeft <= 0 ? "menos de 1 día" : daysLeft + " días") + ".";
                    } else {
                        msg += " Este ban es permanente.";
                    }
                    throw new BusinessException(msg);
                }
            }
        });

        // Autenticar con Spring Security
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, request.getPassword())
            );

            loginAttemptService.loginSucceeded(username);

            String accessToken = jwtTokenProvider.generateAccessToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(username);

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario", "username", username));

            log.info("Login exitoso: {}", user.getUsername());
            return buildAuthResponse(user, accessToken, refreshToken);

        } catch (AuthenticationException e) {
            loginAttemptService.loginFailed(username);
            log.warn("[AUTH_FAIL] Intento fallido para usuario: {}", username);
            throw new BusinessException("Credenciales incorrectas");
        }
    }

    /**
     * Renueva el token de acceso usando un refresh token válido.
     */
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException("Refresh token inválido o expirado");
        }

        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "username", username));

        String newAccessToken = jwtTokenProvider.generateAccessToken(username);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(username);

        return buildAuthResponse(user, newAccessToken, newRefreshToken);
    }

    /**
     * Verifica el email de un usuario mediante el token enviado por correo.
     */
    @Transactional
    public void verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new BusinessException("Token de verificación inválido"));

        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        userRepository.save(user);

        // Enviar email de bienvenida (asíncrono)
        emailService.sendWelcomeEmail(user.getEmail(), user.getUsername());

        log.info("Email verificado para el usuario: {}", user.getUsername());
    }

    /**
     * Reenvía el email de verificación.
     * Genera un nuevo token y envía el correo.
     */
    @Transactional
    public void resendVerificationEmail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", userId));

        if (user.isEmailVerified()) {
            throw new BusinessException("Tu email ya está verificado");
        }

        // Generar nuevo token
        user.setEmailVerificationToken(UUID.randomUUID().toString());
        userRepository.save(user);

        // Reenviar email (asíncrono)
        emailService.sendVerificationEmail(
                user.getEmail(),
                user.getUsername(),
                user.getEmailVerificationToken());

        log.info("Email de verificación reenviado a: {}", user.getEmail());
    }

    /**
     * Genera un código de 6 dígitos y lo envía al email si existe.
     * Siempre devuelve éxito para evitar enumeración de emails.
     */
    @Transactional
    public void forgotPassword(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            String code = String.format("%06d", new java.util.Random().nextInt(1_000_000));
            user.setPasswordResetToken(code);
            user.setPasswordResetTokenExpiry(LocalDateTime.now().plusMinutes(15));
            userRepository.save(user);
            emailService.sendPasswordResetEmail(user.getEmail(), user.getUsername(), code);
            log.info("Código de recuperación enviado a: {}", email);
        });
    }

    /**
     * Valida el código recibido y actualiza la contraseña.
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("Código inválido o expirado"));

        if (user.getPasswordResetToken() == null
                || !user.getPasswordResetToken().equals(request.getCode())
                || user.getPasswordResetTokenExpiry() == null
                || user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Código inválido o expirado");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        userRepository.save(user);

        emailService.sendPasswordChangedEmail(user.getEmail(), user.getUsername());
        log.info("Contraseña restablecida para: {}", user.getEmail());
    }

    // ---- Métodos privados ----

    /**
     * Construye la respuesta de autenticación a partir del usuario y los tokens.
     */
    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .profileImage(user.getProfileImage())
                .role(user.getRole().name())
                .build();
    }
}
