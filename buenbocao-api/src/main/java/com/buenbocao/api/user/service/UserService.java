package com.buenbocao.api.user.service;

import com.buenbocao.api.common.constants.AppConstants;
import com.buenbocao.api.common.exception.BusinessException;
import com.buenbocao.api.common.exception.DuplicateResourceException;
import com.buenbocao.api.common.exception.ResourceNotFoundException;
import com.buenbocao.api.common.exception.UnauthorizedException;
import com.buenbocao.api.common.enums.RecipeStatus;
import com.buenbocao.api.common.enums.SubscriptionStatus;
import com.buenbocao.api.common.response.PagedResponse;
import com.buenbocao.api.email.service.EmailService;
import com.buenbocao.api.recipe.entity.Diet;
import com.buenbocao.api.recipe.repository.DietRepository;
import com.buenbocao.api.recipe.repository.RecipeRepository;
import com.buenbocao.api.security.CustomUserDetails;
import com.buenbocao.api.security.jwt.JwtTokenProvider;
import com.buenbocao.api.social.repository.FollowRepository;
import com.buenbocao.api.social.repository.ReviewRepository;
import com.buenbocao.api.storage.service.StorageService;
import com.buenbocao.api.subscription.repository.SubscriptionRepository;
import com.buenbocao.api.user.dto.request.ChangeEmailRequest;
import com.buenbocao.api.user.dto.request.ChangePasswordRequest;
import com.buenbocao.api.user.dto.request.ChangeUsernameRequest;
import com.buenbocao.api.user.dto.request.UpdateProfileRequest;
import com.buenbocao.api.user.dto.response.UserProfileResponse;
import com.buenbocao.api.user.dto.response.UserSummaryResponse;
import com.buenbocao.api.user.entity.User;
import com.buenbocao.api.user.mapper.UserMapper;
import com.buenbocao.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Servicio de usuario.
 * Contiene toda la lógica de negocio relacionada con el perfil y la cuenta.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final FollowRepository followRepository;
    private final RecipeRepository recipeRepository;
    private final ReviewRepository reviewRepository;
    private final DietRepository dietRepository;
    private final EmailService emailService;
    private final SubscriptionRepository subscriptionRepository;
    private final StorageService storageService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Obtiene el perfil del usuario autenticado.
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(CustomUserDetails currentUser) {
        User user = findUserById(currentUser.getId());
        UserProfileResponse response = userMapper.toProfileResponse(user);
        fillProfileCounters(response, user.getId());

        subscriptionRepository
                .findFirstByUserIdAndStatusOrderByExpiresAtDesc(user.getId(), SubscriptionStatus.ACTIVE)
                .ifPresentOrElse(
                        sub -> response.setSubscriptionPlan(
                                sub.isActive() ? sub.getPlan().name() : "FREE"),
                        () -> response.setSubscriptionPlan("FREE"));

        return response;
    }

    /**
     * Obtiene el perfil público de otro usuario por su username.
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(String username, Long currentUserId) {
        User user = findUserByUsername(username);

        UserProfileResponse response = userMapper.toProfileResponse(user);

        // Ocultar email en perfiles públicos
        response.setEmail(null);
        response.setEmailVerified(false);

        // Rellenar contadores
        fillProfileCounters(response, user.getId());

        // Comprobar si el usuario actual sigue a este perfil
        if (currentUserId != null) {
            response.setFollowedByCurrentUser(
                    followRepository.existsByFollowerIdAndFollowedId(currentUserId, user.getId()));
        }

        return response;
    }

    /**
     * Actualiza los datos del perfil (nombre, apellido, bio).
     */
    @Transactional
    public UserProfileResponse updateProfile(CustomUserDetails currentUser, UpdateProfileRequest request) {
        User user = findUserById(currentUser.getId());

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setBio(request.getBio());

        User updatedUser = userRepository.save(user);
        log.info("Perfil actualizado: {}", updatedUser.getUsername());

        return userMapper.toProfileResponse(updatedUser);
    }

    /**
     * Elimina la foto de perfil del usuario autenticado.
     * Si el usuario no tiene foto, no hace nada (idempotente).
     */
    @Transactional
    public void deleteProfileImage(CustomUserDetails currentUser) {
        User user = findUserById(currentUser.getId());

        if (user.getProfileImage() == null || user.getProfileImage().isBlank()) {
            // Ya no tiene foto, no hay nada que hacer
            return;
        }

        // Borrar el fichero del disco
        try {
            storageService.delete(user.getProfileImage());
        } catch (Exception e) {
            // Si falla el borrado del fichero lo registramos pero no bloqueamos
            log.warn("No se pudo eliminar el fichero de avatar '{}': {}",
                    user.getProfileImage(), e.getMessage());
        }

        user.setProfileImage(null);
        userRepository.save(user);

        log.info("Foto de perfil eliminada para: {}", user.getUsername());
    }

    /**
     * Cambia la contraseña del usuario.
     */
    @Transactional
    public void changePassword(CustomUserDetails currentUser, ChangePasswordRequest request) {
        User user = findUserById(currentUser.getId());

        // Verificar que la contraseña actual es correcta
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException("La contraseña actual es incorrecta");
        }

        // Verificar que la nueva contraseña sea diferente
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BusinessException("La nueva contraseña no puede ser igual a la actual");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Notificar por email (asíncrono)
        emailService.sendPasswordChangedEmail(user.getEmail(), user.getUsername());

        log.info("Contraseña cambiada para el usuario: {}", user.getUsername());
    }

    /**
     * Cambia el email del usuario.
     * Requiere contraseña para confirmar y marca el email como no verificado.
     */
    @Transactional
    public void changeEmail(CustomUserDetails currentUser, ChangeEmailRequest request) {
        User user = findUserById(currentUser.getId());

        // Verificar contraseña
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("La contraseña es incorrecta");
        }

        // Verificar que el nuevo email no esté en uso
        if (userRepository.existsByEmail(request.getNewEmail())) {
            throw new DuplicateResourceException("Usuario", "email", request.getNewEmail());
        }

        // Verificar que no sea el mismo email
        if (user.getEmail().equalsIgnoreCase(request.getNewEmail())) {
            throw new BusinessException("El nuevo email no puede ser igual al actual");
        }

        user.setEmail(request.getNewEmail());
        user.setEmailVerified(false);
        user.setEmailVerificationToken(UUID.randomUUID().toString());
        userRepository.save(user);

        // Enviar email de verificación al nuevo correo (asíncrono)
        emailService.sendVerificationEmail(
                user.getEmail(),
                user.getUsername(),
                user.getEmailVerificationToken());

        log.info("Email cambiado para el usuario: {}. Pendiente de verificación.", user.getUsername());
    }

    /**
     * Cambia el nombre de usuario.
     * Tiene un período de cooldown entre cambios.
     */
    @Transactional
    public Map<String, String> changeUsername(CustomUserDetails currentUser, ChangeUsernameRequest request) {
        User user = findUserById(currentUser.getId());

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("La contraseña es incorrecta");
        }

        if (user.getUsernameLastChangedAt() != null) {
            LocalDateTime nextAllowedChange = user.getUsernameLastChangedAt()
                    .plusDays(AppConstants.USERNAME_CHANGE_COOLDOWN_DAYS);
            if (LocalDateTime.now().isBefore(nextAllowedChange)) {
                long daysRemaining = nextAllowedChange.toLocalDate().toEpochDay() - LocalDate.now().toEpochDay();
                throw new BusinessException(String.format(
                        "Solo puedes cambiar tu nombre de usuario cada %d días. Te quedan %d %s.",
                        AppConstants.USERNAME_CHANGE_COOLDOWN_DAYS,
                        daysRemaining,
                        daysRemaining == 1 ? "día" : "días"));
            }
        }

        if (user.getUsername().equalsIgnoreCase(request.getNewUsername())) {
            throw new BusinessException("El nuevo nombre de usuario no puede ser igual al actual");
        }

        if (userRepository.existsByUsername(request.getNewUsername())) {
            throw new DuplicateResourceException("Usuario", "username", request.getNewUsername());
        }

        user.setUsername(request.getNewUsername());
        user.setUsernameLastChangedAt(LocalDateTime.now());
        userRepository.save(user);

        // Generar nuevos tokens con el username actualizado
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getUsername());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());

        log.info("Username cambiado a: {}", request.getNewUsername());
        return Map.of("accessToken", newAccessToken, "refreshToken", newRefreshToken);
    }

    /**
     * Actualiza la foto de perfil del usuario.
     */
    @Transactional
    public UserProfileResponse updateProfileImage(CustomUserDetails currentUser, String imageUrl) {
        User user = findUserById(currentUser.getId());

        // Si ya tenía foto, borrar la anterior del disco antes de asignar la nueva
        if (user.getProfileImage() != null && !user.getProfileImage().isBlank()) {
            try {
                storageService.delete(user.getProfileImage());
            } catch (Exception e) {
                log.warn("No se pudo eliminar el avatar anterior '{}': {}",
                        user.getProfileImage(), e.getMessage());
            }
        }

        user.setProfileImage(imageUrl);
        User updatedUser = userRepository.save(user);

        log.info("Foto de perfil actualizada para: {}", updatedUser.getUsername());
        return userMapper.toProfileResponse(updatedUser);
    }

    /**
     * Busca usuarios por nombre o username.
     */
    @Transactional(readOnly = true)
    public PagedResponse<UserSummaryResponse> searchUsers(String query, int page, int size) {
        size = Math.min(size, AppConstants.MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, size);

        Page<User> usersPage = userRepository.searchByUsernameOrName(query, pageable);

        List<UserSummaryResponse> content = usersPage.getContent().stream()
                .map(user -> {
                    UserSummaryResponse dto = userMapper.toSummaryResponse(user);
                    dto.setFollowersCount(followRepository.countByFollowedId(user.getId()));
                    dto.setRecipesCount(recipeRepository.countByAuthorIdAndStatus(
                            user.getId(), com.buenbocao.api.common.enums.RecipeStatus.PUBLISHED));
                    return dto;
                })
                .toList();

        return PagedResponse.of(usersPage, content);
    }

    /**
     * Elimina la cuenta del usuario.
     */
    @Transactional
    public void deleteAccount(CustomUserDetails currentUser, String password) {
        User user = findUserById(currentUser.getId());

        // Verificar contraseña para confirmar eliminación
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new UnauthorizedException("La contraseña es incorrecta");
        }

        // Limpiar foto de perfil del disco si existe
        if (user.getProfileImage() != null && !user.getProfileImage().isBlank()) {
            try {
                storageService.delete(user.getProfileImage());
            } catch (Exception e) {
                log.warn("No se pudo eliminar avatar al borrar cuenta '{}': {}",
                        user.getUsername(), e.getMessage());
            }
        }

        userRepository.delete(user);
        log.info("Cuenta eliminada: {}", user.getUsername());
    }

    // ---- Métodos privados de utilidad ----

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "username", username));
    }

    /**
     * Rellena los contadores sociales de un perfil (seguidores, seguidos, recetas).
     */
    private void fillProfileCounters(UserProfileResponse response, Long userId) {
        response.setFollowersCount(followRepository.countByFollowedId(userId));
        response.setFollowingCount(followRepository.countByFollowerId(userId));
        response.setRecipesCount(recipeRepository.countByAuthorIdAndStatus(userId, RecipeStatus.PUBLISHED));
        response.setTotalReviewsReceived(reviewRepository.countTotalReviewsByAuthorId(userId));
    }

    /**
     * Actualiza las preferencias dietéticas del usuario.
     */
    @Transactional
    public Set<String> updateDietaryPreferences(CustomUserDetails currentUser, Set<String> dietNames) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", currentUser.getId()));

        Set<Diet> diets = new java.util.HashSet<>();
        for (String name : dietNames) {
            Diet diet = dietRepository.findByNameIgnoreCase(name.trim())
                    .orElseThrow(() -> new ResourceNotFoundException("Dieta", "nombre", name.trim()));
            diets.add(diet);
        }
        user.setDietaryPreferences(diets);
        userRepository.save(user);

        return diets.stream().map(Diet::getName).collect(java.util.stream.Collectors.toSet());
    }

    /**
     * Obtiene las preferencias dietéticas del usuario.
     */
    @Transactional(readOnly = true)
    public Set<String> getDietaryPreferences(CustomUserDetails currentUser) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", currentUser.getId()));

        return user.getDietaryPreferences().stream()
                .map(Diet::getName)
                .collect(java.util.stream.Collectors.toSet());
    }
}