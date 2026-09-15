package com.buenbocao.api.notification.controller;

import com.buenbocao.api.common.constants.AppConstants;
import com.buenbocao.api.common.response.ApiResponse;
import com.buenbocao.api.common.response.PagedResponse;
import com.buenbocao.api.notification.dto.response.NotificationResponse;
import com.buenbocao.api.notification.service.NotificationService;
import com.buenbocao.api.security.CustomUserDetails;
import com.buenbocao.api.user.entity.User;
import com.buenbocao.api.user.repository.UserRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notificaciones", description = "Sistema de notificaciones")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @Operation(summary = "Mis notificaciones")
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<NotificationResponse>>> getNotifications(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return ResponseEntity.ok(ApiResponse.ok(notificationService.getNotifications(currentUser, page, size)));
    }

    @Operation(summary = "Número de notificaciones sin leer")
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(ApiResponse.ok(notificationService.getUnreadCount(currentUser.getId())));
    }

    @Operation(summary = "Marcar todas como leídas")
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        notificationService.markAllAsRead(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Notificaciones marcadas como leídas"));
    }

    @Operation(summary = "Marcar una notificación como leída")
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long notificationId) {
        notificationService.markAsRead(currentUser.getId(), notificationId);
        return ResponseEntity.ok(ApiResponse.ok("Notificación marcada como leída"));
    }

    @Operation(summary = "Eliminar una notificación")
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long notificationId) {
        notificationService.deleteNotification(currentUser.getId(), notificationId);
        return ResponseEntity.ok(ApiResponse.ok("Notificación eliminada"));
    }

    @Operation(summary = "Eliminar todas mis notificaciones")
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteAllNotifications(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        notificationService.deleteAllNotifications(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Notificaciones eliminadas"));
    }

    @PostMapping("/test")
    public ResponseEntity<?> sendTest(@AuthenticationPrincipal CustomUserDetails currentUser) {
        User user = userRepository.findById(currentUser.getId()).orElseThrow();

        User otherUser = userRepository.findAll().stream()
                .filter(u -> !u.getId().equals(user.getId()))
                .findFirst()
                .orElse(user);

        notificationService.notifyNewReviewToAuthor(otherUser.getId(), user.getId(), 1L, "PRUEBA");
        return ResponseEntity.ok(ApiResponse.ok("Enviada", null));
    }
}
