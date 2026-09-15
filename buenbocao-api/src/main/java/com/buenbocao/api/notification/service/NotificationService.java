package com.buenbocao.api.notification.service;

import com.buenbocao.api.common.constants.AppConstants;
import com.buenbocao.api.common.enums.NotificationType;
import com.buenbocao.api.common.exception.ResourceNotFoundException;
import com.buenbocao.api.common.response.PagedResponse;
import com.buenbocao.api.notification.dto.response.NotificationResponse;
import com.buenbocao.api.notification.entity.Notification;
import com.buenbocao.api.notification.repository.NotificationRepository;
import com.buenbocao.api.security.CustomUserDetails;
import com.buenbocao.api.settings.entity.UserSettings;
import com.buenbocao.api.settings.repository.UserSettingsRepository;
import com.buenbocao.api.user.entity.User;
import com.buenbocao.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final UserSettingsRepository settingsRepository;
    private final WebClient webClient;

    // ==================== ENVÍO ====================

    @Async
    @Transactional
    public void notifyNewFollower(Long followerId, String followerUsername,
            String followerImage, Long followedId) {
        User followed = userRepository.findById(followedId).orElseThrow();
        User follower = userRepository.findById(followerId).orElseThrow();

        Notification notification = Notification.builder()
                .user(followed)
                .type(NotificationType.NEW_FOLLOWER)
                .title("Nuevo seguidor")
                .message(followerUsername + " ha empezado a seguirte")
                .referenceId(followerId)
                .referenceType("Usuario")
                .actorUsername(followerUsername)
                .actorImage(followerImage)
                .build();

        notificationRepository.save(notification);

        messagingTemplate.convertAndSendToUser(
                followed.getUsername(),
                "/queue/notifications",
                toResponse(notification));

        sendPushIfEnabled(followed, NotificationType.NEW_FOLLOWER,
                notification.getMessage(), followerId, follower);
    }

    @Async
    @Transactional
    public void notifyNewReviewToAuthor(Long reviewerId, Long authorId, Long recipeId, String recipeTitle) {
        if (reviewerId.equals(authorId))
            return;
        User reviewer = userRepository.findById(reviewerId).orElseThrow();
        User author = userRepository.findById(authorId).orElseThrow();
        send(author, NotificationType.NEW_REVIEW,
                "Nueva review",
                reviewer.getUsername() + " dejó una review en \"" + truncate(recipeTitle, 50) + "\"",
                recipeId, "Receta", reviewer);
    }

    @Async
    @Transactional
    public void notifyNewFavorite(Long actorId, Long authorId, Long recipeId, String recipeTitle) {
        if (actorId.equals(authorId))
            return;
        User actor = userRepository.findById(actorId).orElseThrow();
        User author = userRepository.findById(authorId).orElseThrow();

        send(author, NotificationType.NEW_FAVORITE,
                "Tu receta fue guardada",
                actor.getUsername() + " guardó \"" + truncate(recipeTitle, 50) + "\" en favoritos",
                recipeId, "Receta", actor);
    }

    @Async
    @Transactional
    public void notifyNewLike(Long actorId, Long authorId, Long recipeId, String recipeTitle) {
        if (actorId.equals(authorId))
            return;
        User actor = userRepository.findById(actorId).orElseThrow();
        User author = userRepository.findById(authorId).orElseThrow();
        send(author, NotificationType.NEW_LIKE,
                "Le gustó tu receta",
                actor.getUsername() + " dio like a \"" + truncate(recipeTitle, 50) + "\"",
                recipeId, "Receta", actor);
    }

    @Async
    @Transactional
    public void notifyRecipePublished(Long authorId, List<Long> followerIds, Long recipeId, String recipeTitle) {
        User author = userRepository.findById(authorId).orElseThrow();
        for (Long followerId : followerIds) {
            User follower = userRepository.findById(followerId).orElseThrow();
            send(follower, NotificationType.RECIPE_PUBLISHED,
                    "Nueva receta",
                    author.getUsername() + " publicó \"" + truncate(recipeTitle, 50) + "\"",
                    recipeId, "Receta", author);
        }
    }

    @Async
    @Transactional
    public void notifySystem(Long userId, String title, String message) {
        User user = userRepository.findById(userId).orElseThrow();
        send(user, NotificationType.SYSTEM, title, message, null, null, null);
    }

    @Async
    @Transactional
    public void notifyReportResolved(Long reporterId, Long reportId, boolean resolved) {
        User reporter = userRepository.findById(reporterId).orElseThrow();
        String statusText = resolved ? "resuelto" : "desestimado";
        String emoji = resolved ? "✅" : "ℹ️";
        send(reporter, NotificationType.REPORT_RESOLVED,
                "Reporte " + statusText,
                emoji + " Tu reporte #" + reportId + " ha sido " + statusText + " por el equipo de moderación.",
                reportId, "Reporte", null);
    }

    @Async
    @Transactional
    public void notifyReportReviewing(Long reporterId, Long reportId) {
        User reporter = userRepository.findById(reporterId).orElseThrow();
        send(reporter, NotificationType.REPORT_REVIEWING,
                "Reporte en revisión",
                "🔍 Tu reporte #" + reportId + " está siendo revisado por el equipo de moderación.",
                reportId, "Reporte", null);
    }

    @Async
    @Transactional
    public void notifyReportDismissed(Long reporterId, Long reportId, String notes) {
        User reporter = userRepository.findById(reporterId).orElseThrow();
        String extra = (notes != null && !notes.isBlank()) ? " Nota: " + notes : "";
        send(reporter, NotificationType.REPORT_DISMISSED,
                "Reporte desestimado",
                "ℹ️ Tu reporte #" + reportId + " ha sido revisado y desestimado." + extra,
                reportId, "Reporte", null);
    }

    @Async
    @Transactional
    public void notifyRecipeUnlocked(Long authorId, String recipeTitle) {
        User author = userRepository.findById(authorId).orElseThrow();
        send(author, NotificationType.SYSTEM,
                "Receta desbloqueada",
                "✅ Tu receta \"" + truncate(recipeTitle, 50)
                        + "\" ha sido revisada y desbloqueada. Ya puedes publicarla de nuevo.",
                null, null, null);
    }

    @Async
    @Transactional
    public void notifyEventReminder(Long userId, Long eventId, String eventTitle) {
        User user = userRepository.findById(userId).orElseThrow();
        send(user, NotificationType.EVENT_REMINDER,
                "Recordatorio de evento",
                "El evento \"" + truncate(eventTitle, 50) + "\" empieza pronto",
                eventId, "Evento", null);
    }

    // ==================== CONSULTAS ====================

    @Transactional(readOnly = true)
    public PagedResponse<NotificationResponse> getNotifications(CustomUserDetails currentUser, int page, int size) {
        size = Math.min(size, AppConstants.MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifPage = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(currentUser.getId(), pageable);
        List<NotificationResponse> content = notifPage.getContent().stream().map(this::toResponse).toList();
        return PagedResponse.of(notifPage, content);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }

    @Transactional
    public void deleteNotification(Long userId, Long notificationId) {
        notificationRepository.deleteByIdAndUserId(notificationId, userId);
    }

    @Transactional
    public void deleteAllNotifications(Long userId) {
        notificationRepository.deleteAllByUserId(userId);
    }

    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación", "id", notificationId));
        if (!notification.getUser().getId().equals(userId))
            return;
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    // ==================== PRIVADO ====================

    private void send(User recipient, NotificationType type, String title, String message,
            Long referenceId, String referenceType, User actor) {

        // Comprobar si el usuario tiene activada esta notificación en app
        Optional<UserSettings> settingsOpt = settingsRepository.findByUserId(recipient.getId());
        UserSettings settings = settingsOpt.orElse(null);

        boolean inAppAllowed = switch (type) {
            case NEW_FOLLOWER -> settings == null || settings.isNotifyNewFollower();
            case NEW_REVIEW -> settings == null || settings.isNotifyNewReview();
            case NEW_FAVORITE, NEW_LIKE -> settings == null || settings.isNotifyNewFavorite();
            case EVENT_REMINDER, EVENT_INVITATION -> true;
            case REPORT_RESOLVED, REPORT_REVIEWING, REPORT_DISMISSED, SYSTEM, SUBSCRIPTION_EXPIRING, RECIPE_PUBLISHED ->
                true;
        };

        if (!inAppAllowed)
            return;

        Notification notification = Notification.builder()
                .user(recipient)
                .type(type)
                .title(title)
                .message(message)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .actorUsername(actor != null ? actor.getUsername() : null)
                .actorImage(actor != null ? actor.getProfileImage() : null)
                .build();
        notificationRepository.save(notification);

        messagingTemplate.convertAndSendToUser(
                recipient.getUsername(),
                "/queue/notifications",
                toResponse(notification));

        sendPushIfEnabled(recipient, type, message, referenceId, actor);
    }

    private void sendPushIfEnabled(User recipient, NotificationType type, String message,
            Long referenceId, User actor) {
        String token = recipient.getExpoPushToken();
        if (token == null || token.isBlank())
            return;

        Optional<UserSettings> settingsOpt = settingsRepository.findByUserId(recipient.getId());
        UserSettings settings = settingsOpt.orElse(null);

        boolean allowed = switch (type) {
            case NEW_FOLLOWER -> true;
            case NEW_REVIEW -> settings == null || settings.isPushNewReview();
            case NEW_FAVORITE -> settings == null || settings.isPushNewFavorite();
            case NEW_LIKE -> settings == null || settings.isPushNewFavorite();
            case RECIPE_PUBLISHED -> settings == null || settings.isPushNewReview();
            case EVENT_REMINDER, EVENT_INVITATION -> true;
            case REPORT_RESOLVED, REPORT_REVIEWING, REPORT_DISMISSED -> true;
            case SYSTEM, SUBSCRIPTION_EXPIRING -> true;
        };

        if (!allowed)
            return;

        sendExpoPush(
                token,
                getTipoAbreviado(type),
                getMensajePush(type, message),
                type,
                referenceId,
                actor != null ? actor.getUsername() : null);
    }

    private void sendExpoPush(String token, String title, String body,
            NotificationType type, Long referenceId, String actorUsername) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("type", type.name());
            if (referenceId != null)
                data.put("referenceId", referenceId.toString());
            if (actorUsername != null)
                data.put("actorUsername", actorUsername);

            switch (type) {
                case NEW_FOLLOWER -> data.put("screen", "UserProfile");
                case NEW_REVIEW, RECIPE_PUBLISHED, NEW_FAVORITE, NEW_LIKE -> data.put("screen", "RecipeDetail");
                case EVENT_REMINDER, EVENT_INVITATION -> data.put("screen", "EventDetail");
                default -> {
                    data.put("stack", "AccountStack");
                    data.put("screen", "NotificationScreen");
                }
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("to", token);
            payload.put("title", title);
            payload.put("body", body);
            payload.put("sound", "default");
            payload.put("data", data);

            webClient.post()
                    .uri("https://exp.host/--/api/v2/push/send")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .subscribe(
                            resp -> log.debug("[PUSH] Enviado a {}: {}", token, resp),
                            err -> log.warn("[PUSH] Error enviando push a {}: {}", token, err.getMessage()));
        } catch (Exception e) {
            log.warn("[PUSH] Excepción enviando push: {}", e.getMessage());
        }
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType().name())
                .title(n.getTitle())
                .message(n.getMessage())
                .referenceId(n.getReferenceId())
                .referenceType(n.getReferenceType())
                .actorUsername(n.getActorUsername())
                .actorImage(n.getActorImage())
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }

    private String truncate(String text, int maxLength) {
        if (text == null)
            return "";
        return text.length() <= maxLength ? text : text.substring(0, maxLength) + "...";
    }

    private String getTipoAbreviado(NotificationType type) {
        return switch (type) {
            case NEW_FOLLOWER -> "Nuevo Seguidor";
            case NEW_REVIEW -> "Nueva Reseña";
            case NEW_FAVORITE -> "Guardado En Favoritos";
            case NEW_LIKE -> "Le Gustó Tu Receta";
            case RECIPE_PUBLISHED -> "Receta Publicada";
            case EVENT_REMINDER -> "Recordatorio";
            case EVENT_INVITATION -> "Invitación";
            case REPORT_RESOLVED -> "Reporte Resuelto";
            case REPORT_REVIEWING -> "Reporte En Revisión";
            case REPORT_DISMISSED -> "Reporte Desestimado";
            case SUBSCRIPTION_EXPIRING -> "Suscripción";
            case SYSTEM -> "Sistema";
        };
    }

    private String getMensajePush(NotificationType type, String message) {
        return switch (type) {
            case NEW_FOLLOWER -> "👤 " + message;
            case NEW_REVIEW -> "💬 " + message;
            case NEW_FAVORITE -> "🔖 " + message;
            case NEW_LIKE -> "❤️ " + message;
            case RECIPE_PUBLISHED -> "📖 " + message;
            case EVENT_REMINDER -> "📅 " + message;
            case EVENT_INVITATION -> "✉️ " + message;
            case REPORT_RESOLVED -> "✅ " + message;
            case REPORT_REVIEWING -> "🔍 " + message;
            case REPORT_DISMISSED -> "ℹ️ " + message;
            case SUBSCRIPTION_EXPIRING -> "⚠️ " + message;
            case SYSTEM -> "🔔 " + message;
        };
    }
}