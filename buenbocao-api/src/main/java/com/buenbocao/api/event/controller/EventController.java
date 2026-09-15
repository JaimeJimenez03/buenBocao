package com.buenbocao.api.event.controller;

import com.buenbocao.api.common.constants.AppConstants;
import com.buenbocao.api.common.enums.EventStatus;
import com.buenbocao.api.common.response.ApiResponse;
import com.buenbocao.api.common.response.PagedResponse;
import com.buenbocao.api.event.dto.request.CreateEventRequest;
import com.buenbocao.api.event.dto.response.EventResponse;
import com.buenbocao.api.event.service.EventService;
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

@RestController
@RequiredArgsConstructor
@Tag(name = "Eventos", description = "Eventos culinarios de la plataforma")
public class EventController {

    private final EventService eventService;

    // ---- ADMIN ----

    @Operation(summary = "[Admin] Crear evento")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/events")
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(
            @AuthenticationPrincipal CustomUserDetails admin,
            @Valid @RequestBody CreateEventRequest request) {
        EventResponse event = eventService.createEvent(admin, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Evento creado", event));
    }

    @Operation(summary = "[Admin] Actualizar evento")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/events/{eventId}")
    public ResponseEntity<ApiResponse<EventResponse>> updateEvent(
            @PathVariable Long eventId,
            @Valid @RequestBody CreateEventRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Evento actualizado", eventService.updateEvent(eventId, request)));
    }

    @Operation(summary = "[Admin] Cambiar estado del evento")
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/admin/events/{eventId}/status")
    public ResponseEntity<ApiResponse<EventResponse>> changeStatus(
            @PathVariable Long eventId, @RequestParam EventStatus status) {
        return ResponseEntity.ok(ApiResponse.ok(eventService.changeEventStatus(eventId, status)));
    }

    @Operation(summary = "[Admin] Eliminar evento")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/events/{eventId}")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(@PathVariable Long eventId) {
        eventService.deleteEvent(eventId);
        return ResponseEntity.ok(ApiResponse.ok("Evento eliminado"));
    }

    // ---- PÚBLICO ----

    @Operation(summary = "Ver evento por ID")
    @GetMapping("/events/{eventId}")
    public ResponseEntity<ApiResponse<EventResponse>> getEvent(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long eventId) {
        Long userId = currentUser != null ? currentUser.getId() : null;
        return ResponseEntity.ok(ApiResponse.ok(eventService.getEvent(eventId, userId)));
    }

    @Operation(summary = "Ver evento por slug")
    @GetMapping("/events/slug/{slug}")
    public ResponseEntity<ApiResponse<EventResponse>> getEventBySlug(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable String slug) {
        Long userId = currentUser != null ? currentUser.getId() : null;
        return ResponseEntity.ok(ApiResponse.ok(eventService.getEventBySlug(slug, userId)));
    }

    @Operation(summary = "Próximos eventos")
    @GetMapping("/events")
    public ResponseEntity<ApiResponse<PagedResponse<EventResponse>>> getUpcomingEvents(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        Long userId = currentUser != null ? currentUser.getId() : null;
        return ResponseEntity.ok(ApiResponse.ok(eventService.getUpcomingEvents(userId, page, size)));
    }

    @Operation(summary = "Mis eventos (en los que estoy inscrito)")
    @GetMapping("/events/me")
    public ResponseEntity<ApiResponse<PagedResponse<EventResponse>>> getMyEvents(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return ResponseEntity.ok(ApiResponse.ok(eventService.getMyEvents(currentUser.getId(), page, size)));
    }

    // ---- PARTICIPACIÓN ----

    @Operation(summary = "Inscribirse en un evento")
    @PostMapping("/events/{eventId}/join")
    public ResponseEntity<ApiResponse<EventResponse>> joinEvent(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long eventId) {
        return ResponseEntity.ok(ApiResponse.ok("Inscrito al evento", eventService.joinEvent(currentUser, eventId)));
    }

    @Operation(summary = "Desinscribirse de un evento")
    @DeleteMapping("/events/{eventId}/leave")
    public ResponseEntity<ApiResponse<EventResponse>> leaveEvent(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long eventId) {
        return ResponseEntity.ok(ApiResponse.ok("Desinscrito del evento", eventService.leaveEvent(currentUser, eventId)));
    }
}
