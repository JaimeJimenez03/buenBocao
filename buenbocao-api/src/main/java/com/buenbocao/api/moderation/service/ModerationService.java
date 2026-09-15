package com.buenbocao.api.moderation.service;

import com.buenbocao.api.common.constants.AppConstants;
import com.buenbocao.api.common.enums.ReportReason;
import com.buenbocao.api.common.enums.ReportStatus;
import com.buenbocao.api.common.enums.ReportTargetType;
import com.buenbocao.api.common.exception.BusinessException;
import com.buenbocao.api.common.exception.ResourceNotFoundException;
import com.buenbocao.api.common.response.PagedResponse;
import com.buenbocao.api.moderation.dto.request.CreateReportRequest;
import com.buenbocao.api.moderation.dto.request.ResolveReportRequest;
import com.buenbocao.api.moderation.dto.response.ModerationStatsResponse;
import com.buenbocao.api.moderation.dto.response.ReportResponse;
import com.buenbocao.api.moderation.entity.Report;
import com.buenbocao.api.moderation.repository.ReportRepository;
import com.buenbocao.api.recipe.repository.RecipeRepository;
import com.buenbocao.api.security.CustomUserDetails;
import com.buenbocao.api.social.repository.ReviewRepository;
import com.buenbocao.api.user.entity.User;
import com.buenbocao.api.notification.service.NotificationService;
import com.buenbocao.api.user.repository.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ModerationService {

        private final ReportRepository reportRepository;
        private final UserRepository userRepository;
        private final RecipeRepository recipeRepository;
        private final ReviewRepository reviewRepository;
        private final NotificationService notificationService;
        private final SimpMessagingTemplate messagingTemplate;
        private final com.buenbocao.api.email.service.EmailService emailService;

        // ===================== USUARIO =====================

        @Transactional
        public ReportResponse createReport(CustomUserDetails currentUser, CreateReportRequest request) {
                User reporter = userRepository.findById(currentUser.getId())
                                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", currentUser.getId()));

                // El dueño de la receta puede enviar revisiones sin límite.
                // Cualquier otro usuario solo puede reportar una vez el mismo contenido.
                boolean isOwner = false;
                if (request.getTargetType() == ReportTargetType.RECIPE) {
                        isOwner = recipeRepository.findById(request.getTargetId())
                                        .map(r -> r.getAuthor().getId().equals(currentUser.getId()))
                                        .orElse(false);
                }

                if (!isOwner && reportRepository.existsByReporterIdAndTargetTypeAndTargetId(
                                currentUser.getId(), request.getTargetType(), request.getTargetId())) {
                        throw new BusinessException("Ya has reportado este contenido anteriormente.");
                }

                // Si es owner y ya tiene un reporte previo sobre este contenido,
                // actualizarlo en lugar de insertar uno nuevo para respetar el unique
                // constraint de la BD (reporter_id, target_type, target_id).
                Report report;
                if (isOwner) {
                        report = reportRepository
                                        .findByReporterIdAndTargetTypeAndTargetId(
                                                        currentUser.getId(), request.getTargetType(),
                                                        request.getTargetId())
                                        .orElse(null);
                        if (report != null) {
                                // Actualizar el reporte existente con los nuevos datos
                                report.setReasons(request.getReasons());
                                report.setDescription(request.getDescription());
                                report.setStatus(ReportStatus.PENDING);
                                report.setModeratorNotes(null);
                                report.setResolvedBy(null);
                        }
                } else {
                        report = null;
                }

                if (report == null) {
                        report = Report.builder()
                                        .reporter(reporter)
                                        .targetType(request.getTargetType())
                                        .targetId(request.getTargetId())
                                        .reasons(request.getReasons())
                                        .description(request.getDescription())
                                        .build();
                }

                Report saved = reportRepository.save(report);
                messagingTemplate.convertAndSend("/topic/admin/reports", toResponse(saved));

                return toResponse(saved);
        }

        // ===================== ADMIN =====================

        @Transactional(readOnly = true)
        public PagedResponse<ReportResponse> getPendingReports(int page, int size) {
                size = Math.min(size, AppConstants.MAX_PAGE_SIZE);
                Pageable pageable = PageRequest.of(page, size);
                Page<Report> reportsPage = reportRepository.findByStatusOrderByCreatedAtAsc(ReportStatus.PENDING,
                                pageable);
                List<ReportResponse> content = reportsPage.getContent().stream().map(this::toResponse).toList();
                return PagedResponse.of(reportsPage, content);
        }

        @Transactional(readOnly = true)
        public PagedResponse<ReportResponse> getAllReports(int page, int size,
                        ReportStatus status,
                        ReportReason reason,
                        ReportTargetType targetType) {
                size = Math.min(size, AppConstants.MAX_PAGE_SIZE);
                Pageable pageable = PageRequest.of(page, size);
                Page<Report> reportsPage = reportRepository.findWithFilters(status, reason, targetType, pageable);
                List<ReportResponse> content = reportsPage.getContent().stream().map(this::toResponse).toList();
                return PagedResponse.of(reportsPage, content);
        }

        @Transactional(readOnly = true)
        public ReportResponse getReport(Long reportId) {
                Report report = reportRepository.findById(reportId)
                                .orElseThrow(() -> new ResourceNotFoundException("Reporte", "id", reportId));
                return toResponse(report);
        }

        @Transactional
        public ReportResponse resolveReport(CustomUserDetails admin, Long reportId, ResolveReportRequest request) {
                Report report = reportRepository.findById(reportId)
                                .orElseThrow(() -> new ResourceNotFoundException("Reporte", "id", reportId));

                User moderator = userRepository.findById(admin.getId())
                                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", admin.getId()));

                report.setStatus(request.getStatus());
                report.setModeratorNotes(request.getModeratorNotes());
                report.setResolvedBy(moderator);

                // Extraer ID antes del save para evitar proxy Hibernate en @Async
                Long reporterId = report.getReporter().getId();
                Report updated = reportRepository.save(report);
                log.info("Reporte #{} resuelto como {} por admin {}", reportId, request.getStatus(),
                                admin.getUsername());

                if (request.getStatus() == ReportStatus.RESOLVED) {
                        notificationService.notifyReportResolved(reporterId, reportId, true);
                } else if (request.getStatus() == ReportStatus.DISMISSED) {
                        notificationService.notifyReportDismissed(reporterId, reportId,
                                        request.getModeratorNotes());
                }
                messagingTemplate.convertAndSend("/topic/admin/reports", toResponse(updated));

                return toResponse(updated);
        }

        @Transactional
        public ReportResponse markAsReviewing(CustomUserDetails admin, Long reportId) {
                Report report = reportRepository.findById(reportId)
                                .orElseThrow(() -> new ResourceNotFoundException("Reporte", "id", reportId));

                if (report.getStatus() != ReportStatus.PENDING) {
                        throw new BusinessException("Solo se pueden marcar como 'en revisión' los reportes pendientes");
                }

                User moderator = userRepository.findById(admin.getId())
                                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", admin.getId()));

                report.setStatus(ReportStatus.REVIEWING);
                report.setResolvedBy(moderator);

                // Guardar el ID del reporter antes del save para evitar proxy Hibernate en
                // @Async
                Long reporterId = report.getReporter().getId();
                Report updated = reportRepository.save(report);
                // Notificar al reporter que su reporte está siendo revisado
                notificationService.notifyReportReviewing(reporterId, reportId);
                log.info("Reporte #{} marcado como REVIEWING por admin {}", reportId, admin.getUsername());
                return toResponse(updated);
        }

        @Transactional
        public void banUserFromReport(CustomUserDetails admin, Long reportId, String banReason, Integer durationDays) {
                Report report = reportRepository.findById(reportId)
                                .orElseThrow(() -> new ResourceNotFoundException("Reporte", "id", reportId));

                Long targetUserId;
                if (report.getTargetType() == ReportTargetType.USER) {
                        targetUserId = report.getTargetId();
                } else if (report.getTargetType() == ReportTargetType.RECIPE) {
                        targetUserId = recipeRepository.findById(report.getTargetId())
                                        .map(r -> r.getAuthor().getId())
                                        .orElseThrow(() -> new ResourceNotFoundException("Receta", "id",
                                                        report.getTargetId()));
                } else if (report.getTargetType() == ReportTargetType.REVIEW) {
                        targetUserId = reviewRepository.findById(report.getTargetId())
                                        .map(r -> r.getUser().getId())
                                        .orElseThrow(() -> new ResourceNotFoundException("Review", "id",
                                                        report.getTargetId()));
                } else {
                        throw new BusinessException("Tipo de contenido no soportado para banear");
                }

                User user = userRepository.findById(targetUserId)
                                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", targetUserId));

                if (!user.isEnabled()) {
                        throw new BusinessException("El usuario ya está baneado");
                }

                String finalReason = (banReason != null && !banReason.isBlank()) ? banReason
                                : "Violación de las normas de la comunidad";
                LocalDateTime expiresAt = durationDays != null ? LocalDateTime.now().plusDays(durationDays) : null;

                user.setEnabled(false);
                user.setBanReason(finalReason);
                user.setBanExpiresAt(expiresAt);
                userRepository.save(user);

                notificationService.notifySystem(user.getId(), "Cuenta suspendida",
                                "Tu cuenta ha sido suspendida. Motivo: " + finalReason +
                                                (durationDays != null ? ". Duración: " + durationDays + " días."
                                                                : ". Ban permanente."));
                emailService.sendBanEmail(user.getEmail(), user.getUsername(), finalReason, durationDays);

                User moderator = userRepository.findById(admin.getId())
                                .orElseThrow(() -> new ResourceNotFoundException("Admin", "id", admin.getId()));
                report.setStatus(ReportStatus.RESOLVED);
                report.setResolvedBy(moderator);
                String banNote = "Usuario baneado. Motivo: " + finalReason +
                                (durationDays != null ? " | Duración: " + durationDays + " días" : " | Ban permanente");
                report.setModeratorNotes(banNote);
                reportRepository.save(report);

                log.info("Usuario {} baneado por admin {} (reporte #{}). Motivo: {}. Días: {}",
                                user.getUsername(), admin.getUsername(), reportId, finalReason, durationDays);
        }

        @Transactional
        public void deleteReportedContent(CustomUserDetails admin, Long reportId) {
                deleteReportedContent(admin, reportId, null);
        }

        @Transactional
        public void deleteReportedContent(CustomUserDetails admin, Long reportId, String reason) {
                Report report = reportRepository.findById(reportId)
                                .orElseThrow(() -> new ResourceNotFoundException("Reporte", "id", reportId));

                switch (report.getTargetType()) {
                        case RECIPE -> {
                                // Las recetas NUNCA se eliminan: se archivan y bloquean por admin
                                com.buenbocao.api.recipe.entity.Recipe recipe = recipeRepository
                                                .findById(report.getTargetId())
                                                .orElseThrow(() -> new ResourceNotFoundException("Receta", "id",
                                                                report.getTargetId()));
                                recipe.setStatus(com.buenbocao.api.common.enums.RecipeStatus.LOCKED);
                                recipeRepository.save(recipe);
                                String notifMsg = "Tu receta \"" + recipe.getTitle()
                                                + "\" ha sido bloqueada por moderación."
                                                + (reason != null && !reason.isBlank() ? " Motivo: " + reason : "")
                                                + " Puedes solicitar una revision desde la seccion de tus recetas.";
                                notificationService.notifySystem(recipe.getAuthor().getId(),
                                                "Receta bloqueada por moderación",
                                                notifMsg);
                        }
                        case REVIEW ->
                                reviewRepository.findById(report.getTargetId()).ifPresent(reviewRepository::delete);
                        default -> throw new BusinessException(
                                        "No se puede procesar contenido de tipo " + report.getTargetType());
                }

                User moderator = userRepository.findById(admin.getId())
                                .orElseThrow(() -> new ResourceNotFoundException("Admin", "id", admin.getId()));
                report.setStatus(ReportStatus.RESOLVED);
                report.setResolvedBy(moderator);
                String notes = reason != null && !reason.isBlank()
                                ? reason
                                : "Archivado por moderacion sin motivo especificado.";
                report.setModeratorNotes(notes);
                reportRepository.save(report);

                log.info("Contenido {} #{} archivado/eliminado por admin {} desde reporte #{} — motivo: {}",
                                report.getTargetType(), report.getTargetId(), admin.getUsername(), reportId,
                                notes);
        }

        @Transactional
        public void unbanUser(CustomUserDetails admin, Long userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", userId));

                if (user.isEnabled()) {
                        throw new BusinessException("El usuario no está baneado");
                }

                user.setEnabled(true);
                user.setBanReason(null);
                user.setBanExpiresAt(null);
                userRepository.save(user);
                log.info("Usuario {} desbaneado por admin {}", user.getUsername(), admin.getUsername());
        }

        @Transactional(readOnly = true)
        public List<String> getBannedUsernames() {
                return userRepository.findByEnabledFalse().stream()
                                .map(User::getUsername)
                                .toList();
        }

        @Transactional(readOnly = true)
        public PagedResponse<ReportResponse> getMyReports(CustomUserDetails currentUser, int page, int size) {
                size = Math.min(size, AppConstants.MAX_PAGE_SIZE);
                Pageable pageable = PageRequest.of(page, size);
                Page<Report> reportsPage = reportRepository.findByReporterIdOrderByCreatedAtDesc(currentUser.getId(),
                                pageable);
                List<ReportResponse> content = reportsPage.getContent().stream().map(this::toResponse).toList();
                return PagedResponse.of(reportsPage, content);
        }

        @Transactional(readOnly = true)
        public ModerationStatsResponse getStats() {
                return ModerationStatsResponse.builder()
                                .pendingReports(reportRepository.countByStatus(ReportStatus.PENDING))
                                .reviewingReports(reportRepository.countByStatus(ReportStatus.REVIEWING))
                                .resolvedReports(reportRepository.countByStatus(ReportStatus.RESOLVED))
                                .dismissedReports(reportRepository.countByStatus(ReportStatus.DISMISSED))
                                .totalReports(reportRepository.count())
                                .bannedUsers(userRepository.countByEnabledFalse())
                                .build();
        }

        /**
         * Elimina un reporte propio, solo si está RESOLVED o DISMISSED.
         */
        @Transactional
        public void deleteMyReport(CustomUserDetails currentUser, Long reportId) {
                Report report = reportRepository.findById(reportId)
                                .orElseThrow(() -> new ResourceNotFoundException("Reporte", "id", reportId));

                if (!report.getReporter().getId().equals(currentUser.getId())) {
                        throw new com.buenbocao.api.common.exception.UnauthorizedException(
                                        "No puedes eliminar este reporte");
                }

                if (report.getStatus() != ReportStatus.RESOLVED
                                && report.getStatus() != ReportStatus.DISMISSED) {
                        throw new BusinessException(
                                        "Solo puedes eliminar reportes resueltos o desestimados");
                }

                reportRepository.delete(report);
                log.info("Reporte #{} eliminado por el usuario {}", reportId, currentUser.getUsername());
        }

        @Transactional(readOnly = true)
        public boolean hasAlreadyReported(CustomUserDetails currentUser,
                        com.buenbocao.api.common.enums.ReportTargetType targetType, Long targetId) {
                return reportRepository.existsByReporterIdAndTargetTypeAndTargetId(
                                currentUser.getId(), targetType, targetId);
        }

        /**
         * Obtiene todos los reportes sobre el mismo contenido (para contexto en el
         * panel admin).
         */
        @Transactional(readOnly = true)
        public List<ReportResponse> getReportsByTarget(ReportTargetType targetType, Long targetId) {
                return reportRepository.findByTargetTypeAndTargetIdOrderByCreatedAtDesc(targetType, targetId)
                                .stream().map(this::toResponse).toList();
        }

        // ===================== PRIVADO =====================

        private ReportResponse toResponse(Report report) {
                // Resolver nombre y autor del contenido reportado
                String targetName = resolveTargetName(report.getTargetType(), report.getTargetId());
                String targetAuthorUsername = resolveTargetAuthor(report.getTargetType(), report.getTargetId());

                List<String> reasonNames = report.getReasons() != null
                                ? report.getReasons().stream().map(ReportReason::name).toList()
                                : List.of();

                return ReportResponse.builder()
                                .id(report.getId())
                                .reporterUsername(report.getReporter().getUsername())
                                .targetType(report.getTargetType().name())
                                .targetId(report.getTargetId())
                                .targetName(targetName)
                                .targetAuthorUsername(targetAuthorUsername)
                                .reasons(reasonNames)
                                .description(report.getDescription())
                                .status(report.getStatus().name())
                                .moderatorNotes(report.getModeratorNotes())
                                .resolvedByUsername(
                                                report.getResolvedBy() != null ? report.getResolvedBy().getUsername()
                                                                : null)
                                .createdAt(report.getCreatedAt())
                                .updatedAt(report.getUpdatedAt())
                                .build();
        }

        public ReportResponse toPublicResponse(Report report) {
                return toResponse(report);
        }

        private String resolveTargetName(ReportTargetType type, Long targetId) {
                try {
                        return switch (type) {
                                case RECIPE -> recipeRepository.findById(targetId)
                                                .map(r -> r.getTitle())
                                                .orElse("Receta #" + targetId);
                                case USER -> userRepository.findById(targetId)
                                                .map(u -> "@" + u.getUsername())
                                                .orElse("Usuario #" + targetId);
                                case REVIEW -> reviewRepository.findById(targetId)
                                                .map(r -> "Reseña de @" + r.getUser().getUsername())
                                                .orElse("Reseña #" + targetId);
                        };
                } catch (Exception e) {
                        return type.name() + " #" + targetId;
                }
        }

        private String resolveTargetAuthor(ReportTargetType type, Long targetId) {
                try {
                        return switch (type) {
                                case RECIPE -> recipeRepository.findById(targetId)
                                                .map(r -> "@" + r.getAuthor().getUsername())
                                                .orElse(null);
                                case REVIEW -> reviewRepository.findById(targetId)
                                                .map(r -> "@" + r.getUser().getUsername())
                                                .orElse(null);
                                case USER -> null; // el target ya es el usuario
                        };
                } catch (Exception e) {
                        return null;
                }
        }
}