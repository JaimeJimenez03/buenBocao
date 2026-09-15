package com.buenbocao.api.moderation.controller;

import com.buenbocao.api.common.constants.AppConstants;
import com.buenbocao.api.common.enums.ReportReason;
import com.buenbocao.api.common.enums.ReportStatus;
import com.buenbocao.api.common.enums.ReportTargetType;
import com.buenbocao.api.common.response.ApiResponse;
import com.buenbocao.api.common.response.PagedResponse;
import com.buenbocao.api.moderation.dto.request.CreateReportRequest;
import com.buenbocao.api.moderation.dto.request.ResolveReportRequest;
import com.buenbocao.api.moderation.dto.response.ModerationStatsResponse;
import com.buenbocao.api.moderation.dto.response.ReportResponse;
import com.buenbocao.api.moderation.service.ModerationService;
import com.buenbocao.api.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Moderación", description = "Reportes y panel de administración")
public class ModerationController {

    private final ModerationService moderationService;

    // ===================== USUARIO =====================

    @Operation(summary = "Reportar contenido")
    @PostMapping("/reports")
    public ResponseEntity<ApiResponse<ReportResponse>> createReport(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody CreateReportRequest request) {
        ReportResponse report = moderationService.createReport(currentUser, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Reporte enviado", report));
    }

    @Operation(summary = "Eliminar un reporte propio (solo RESOLVED o DISMISSED)")
    @DeleteMapping("/reports/{reportId}")
    public ResponseEntity<ApiResponse<Void>> deleteMyReport(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long reportId) {
        moderationService.deleteMyReport(currentUser, reportId);
        return ResponseEntity.ok(ApiResponse.ok("Reporte eliminado"));
    }

    @Operation(summary = "Comprobar si ya has reportado un contenido")
    @GetMapping("/reports/check")
    public ResponseEntity<ApiResponse<java.util.Map<String, Boolean>>> checkAlreadyReported(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam com.buenbocao.api.common.enums.ReportTargetType targetType,
            @RequestParam Long targetId) {

        // El owner nunca esta bloqueado: RECIPE (autor) se gestiona en el servicio;
        // USER: el propio usuario puede reportar su perfil sin limite.
        boolean isOwner = (targetType == ReportTargetType.USER && targetId.equals(currentUser.getId()));
        boolean already = !isOwner && moderationService.hasAlreadyReported(currentUser, targetType, targetId);
        return ResponseEntity.ok(ApiResponse.ok(java.util.Map.of("alreadyReported", already)));
    }

    @GetMapping("/reports/mine")
    public ResponseEntity<ApiResponse<PagedResponse<ReportResponse>>> getMyReports(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return ResponseEntity.ok(ApiResponse.ok(moderationService.getMyReports(currentUser, page, size)));
    }

    @Operation(summary = "Obtener motivos de reporte disponibles")
    @GetMapping("/reports/reasons")
    public ResponseEntity<ApiResponse<List<String>>> getReportReasons() {
        List<String> reasons = Arrays.stream(ReportReason.values())
                .map(Enum::name)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(reasons));
    }

    @Operation(summary = "Obtener tipos de contenido reportable")
    @GetMapping("/reports/target-types")
    public ResponseEntity<ApiResponse<List<String>>> getTargetTypes() {
        List<String> types = Arrays.stream(ReportTargetType.values())
                .map(Enum::name)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(types));
    }

    // ===================== ADMIN =====================

    @Operation(summary = "[Admin] Reportes pendientes")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/reports/pending")
    public ResponseEntity<ApiResponse<PagedResponse<ReportResponse>>> getPendingReports(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return ResponseEntity.ok(ApiResponse.ok(moderationService.getPendingReports(page, size)));
    }

    @Operation(summary = "[Admin] Todos los reportes con filtros opcionales")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/reports")
    public ResponseEntity<ApiResponse<PagedResponse<ReportResponse>>> getAllReports(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(required = false) ReportReason reason,
            @RequestParam(required = false) ReportTargetType targetType) {
        return ResponseEntity.ok(ApiResponse.ok(
                moderationService.getAllReports(page, size, status, reason, targetType)));
    }

    @Operation(summary = "[Admin] Detalle de un reporte")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/reports/{reportId}")
    public ResponseEntity<ApiResponse<ReportResponse>> getReport(
            @PathVariable Long reportId) {
        return ResponseEntity.ok(ApiResponse.ok(moderationService.getReport(reportId)));
    }

    @Operation(summary = "[Admin] Actualizar estado de un reporte (resolver, revisar, desestimar)")
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/admin/reports/{reportId}")
    public ResponseEntity<ApiResponse<ReportResponse>> resolveReport(
            @AuthenticationPrincipal CustomUserDetails admin,
            @PathVariable Long reportId,
            @Valid @RequestBody ResolveReportRequest request) {
        ReportResponse report = moderationService.resolveReport(admin, reportId, request);
        return ResponseEntity.ok(ApiResponse.ok("Reporte actualizado", report));
    }

    @Operation(summary = "[Admin] Marcar reporte como 'en revisión'")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/reports/{reportId}/reviewing")
    public ResponseEntity<ApiResponse<ReportResponse>> markAsReviewing(
            @AuthenticationPrincipal CustomUserDetails admin,
            @PathVariable Long reportId) {
        ReportResponse report = moderationService.markAsReviewing(admin, reportId);
        return ResponseEntity.ok(ApiResponse.ok("Reporte marcado como en revisión", report));
    }

    @Operation(summary = "[Admin] Banear usuario reportado")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/reports/{reportId}/ban-user")
    public ResponseEntity<ApiResponse<Void>> banUserFromReport(
            @AuthenticationPrincipal CustomUserDetails admin,
            @PathVariable Long reportId,
            @RequestBody(required = false) com.buenbocao.api.moderation.dto.request.BanRequest banRequest) {
        String reason = banRequest != null ? banRequest.getReason() : null;
        Integer durationDays = banRequest != null ? banRequest.getDurationDays() : null;
        moderationService.banUserFromReport(admin, reportId, reason, durationDays);
        return ResponseEntity.ok(ApiResponse.ok("Usuario baneado", null));
    }

    @Operation(summary = "[Admin] Banear usuario directamente")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/users/{userId}/ban")
    public ResponseEntity<ApiResponse<Void>> banUser(
            @AuthenticationPrincipal CustomUserDetails admin,
            @PathVariable Long userId,
            @RequestBody(required = false) com.buenbocao.api.moderation.dto.request.BanRequest banRequest) {
        String reason = banRequest != null ? banRequest.getReason() : null;
        Integer durationDays = banRequest != null ? banRequest.getDurationDays() : null;
        // moderationService.banUser(admin, userId, reason, durationDays);
        return ResponseEntity.ok(ApiResponse.ok("Usuario baneado", null));
    }

    @Operation(summary = "[Admin] Eliminar contenido reportado")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/reports/{reportId}/content")
    public ResponseEntity<ApiResponse<Void>> deleteReportedContent(
            @AuthenticationPrincipal CustomUserDetails admin,
            @PathVariable Long reportId) {
        moderationService.deleteReportedContent(admin, reportId);
        return ResponseEntity.ok(ApiResponse.ok("Contenido eliminado", null));
    }

    @Operation(summary = "[Admin] Estadísticas de moderación")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/reports/stats")
    public ResponseEntity<ApiResponse<ModerationStatsResponse>> getStats() {
        return ResponseEntity.ok(ApiResponse.ok(moderationService.getStats()));
    }

    @Operation(summary = "[Admin] Listar usuarios baneados")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/users/banned")
    public ResponseEntity<ApiResponse<List<String>>> getBannedUsers() {
        return ResponseEntity.ok(ApiResponse.ok(moderationService.getBannedUsernames()));
    }

    @Operation(summary = "[Admin] Desbanear usuario")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/users/{userId}/unban")
    public ResponseEntity<ApiResponse<Void>> unbanUser(
            @AuthenticationPrincipal CustomUserDetails admin,
            @PathVariable Long userId) {
        moderationService.unbanUser(admin, userId);
        return ResponseEntity.ok(ApiResponse.ok("Usuario desbaneado", null));
    }
}