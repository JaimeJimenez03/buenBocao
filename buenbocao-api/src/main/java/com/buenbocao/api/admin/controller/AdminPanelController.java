package com.buenbocao.api.admin.controller;

import com.buenbocao.api.common.enums.*;
import com.buenbocao.api.event.dto.request.CreateEventRequest;
import com.buenbocao.api.event.entity.Event;
import com.buenbocao.api.event.repository.EventRepository;
import com.buenbocao.api.event.service.EventService;
import com.buenbocao.api.moderation.dto.request.ResolveReportRequest;
import com.buenbocao.api.moderation.dto.response.ModerationStatsResponse;
import com.buenbocao.api.moderation.dto.response.ReportResponse;
import com.buenbocao.api.moderation.repository.ReportRepository;
import com.buenbocao.api.moderation.service.ModerationService;
import com.buenbocao.api.recipe.entity.Recipe;
import com.buenbocao.api.recipe.repository.RecipeRepository;
import com.buenbocao.api.recipe.service.RecipeService;
import com.buenbocao.api.security.CustomUserDetails;
import com.buenbocao.api.subscription.entity.Subscription;
import com.buenbocao.api.subscription.repository.SubscriptionRepository;
import com.buenbocao.api.user.entity.User;
import com.buenbocao.api.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Hidden
@Controller
@RequestMapping("/adminControlPanel")
@RequiredArgsConstructor
public class AdminPanelController {

    private final AuthenticationManager authenticationManager;
    private final ModerationService moderationService;
    private final RecipeService recipeService;
    private final EventService eventService;
    private final com.buenbocao.api.storage.service.StorageService storageService;
    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;
    private final EventRepository eventRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final ReportRepository reportRepository;

    // ════════════════════════════════════════════════
    // AUTH
    // ════════════════════════════════════════════════

    @GetMapping("/login")
    public String loginForm(@RequestParam(required = false) String error, Model model) {
        if (error != null)
            model.addAttribute("error", "Credenciales incorrectas o sin permisos de administrador.");
        return "admin/login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            RedirectAttributes ra) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (!isAdmin) {
                ra.addAttribute("error", "true");
                return "redirect:/adminControlPanel/login";
            }
            SecurityContextHolder.getContext().setAuthentication(auth);
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
            return "redirect:/adminControlPanel";
        } catch (AuthenticationException e) {
            ra.addAttribute("error", "true");
            return "redirect:/adminControlPanel/login";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        SecurityContextHolder.clearContext();
        return "redirect:/adminControlPanel/login";
    }

    // ════════════════════════════════════════════════
    // DASHBOARD
    // ════════════════════════════════════════════════

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails admin, Model model) {
        ModerationStatsResponse stats = moderationService.getStats();

        long totalUsers = userRepository.count();
        long totalRecipes = recipeRepository.count();
        long publishedRecipes = recipeRepository.countByStatus(RecipeStatus.PUBLISHED);
        long archivedRecipes = recipeRepository.countByStatus(RecipeStatus.ARCHIVED);
        long lockedRecipes = recipeRepository.countByStatus(RecipeStatus.LOCKED);
        long totalEvents = eventRepository.count();
        long upcomingEvents = eventRepository.countByStatus(EventStatus.UPCOMING);
        long activeSubs = subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE);

        // Últimos 5 reportes pendientes para el dashboard
        var latestReports = moderationService.getPendingReports(0, 5);

        model.addAttribute("adminUsername", admin.getUsername());
        model.addAttribute("stats", stats);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalRecipes", totalRecipes);
        model.addAttribute("publishedRecipes", publishedRecipes);
        model.addAttribute("archivedRecipes", archivedRecipes);
        model.addAttribute("lockedRecipes", lockedRecipes);
        model.addAttribute("totalEvents", totalEvents);
        model.addAttribute("upcomingEvents", upcomingEvents);
        model.addAttribute("activeSubs", activeSubs);
        model.addAttribute("latestReports", latestReports.getContent());
        model.addAttribute("activePage", "dashboard");
        return "admin/dashboard";
    }

    // ════════════════════════════════════════════════
    // REPORTES
    // ════════════════════════════════════════════════

    @GetMapping("/reports")
    @PreAuthorize("hasRole('ADMIN')")
    public String reports(@AuthenticationPrincipal CustomUserDetails admin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String reason,
            @RequestParam(required = false) String targetType,
            Model model) {
        ReportStatus statusEnum = parse(status, ReportStatus.class);
        ReportReason reasonEnum = parse(reason, ReportReason.class);
        ReportTargetType targetEnum = parse(targetType, ReportTargetType.class);

        var reports = moderationService.getAllReports(page, 20, statusEnum, reasonEnum, targetEnum);

        model.addAttribute("adminUsername", admin.getUsername());
        model.addAttribute("reports", reports);
        model.addAttribute("currentPage", page);
        model.addAttribute("filterStatus", status);
        model.addAttribute("filterReason", reason);
        model.addAttribute("filterTarget", targetType);
        model.addAttribute("statuses", ReportStatus.values());
        model.addAttribute("reasons", ReportReason.values());
        model.addAttribute("targetTypes", ReportTargetType.values());
        model.addAttribute("activePage", "reports");
        return "admin/reports";
    }

    @GetMapping("/my-queue")
    @PreAuthorize("hasRole('ADMIN')")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public String myQueue(@AuthenticationPrincipal CustomUserDetails admin, Model model) {

        List<ReportResponse> myQueue = reportRepository
                .findByResolvedByIdAndStatusOrderByUpdatedAtAsc(
                        admin.getId(), ReportStatus.REVIEWING)
                .stream()
                .map(moderationService::toPublicResponse)
                .toList();

        // Conteos por tipo calculados en servidor: los streams de Java no funcionan
        // directamente en expresiones Thymeleaf/SpEL
        long countRecipes = myQueue.stream().filter(r -> "RECIPE".equals(r.getTargetType())).count();
        long countUsers = myQueue.stream().filter(r -> "USER".equals(r.getTargetType())).count();
        long countReviews = myQueue.stream().filter(r -> "REVIEW".equals(r.getTargetType())).count();

        model.addAttribute("adminUsername", admin.getUsername());
        model.addAttribute("myQueue", myQueue);
        model.addAttribute("myQueueCountRecipes", countRecipes);
        model.addAttribute("myQueueCountUsers", countUsers);
        model.addAttribute("myQueueCountReviews", countReviews);
        model.addAttribute("activePage", "my-queue");
        return "admin/my-queue";
    }

    /**
     * Inyecta myQueueCount en todos los modelos del panel para que el sidebar
     * muestre el badge con el número de reportes en revisión del admin activo,
     * independientemente de la página que se esté renderizando.
     */
    @ModelAttribute
    public void addGlobalAttributes(@AuthenticationPrincipal CustomUserDetails admin, Model model) {
        if (admin == null)
            return;
        try {
            long count = reportRepository
                    .findByResolvedByIdAndStatusOrderByUpdatedAtAsc(admin.getId(), ReportStatus.REVIEWING)
                    .size();
            model.addAttribute("myQueueCount", count);
        } catch (Exception e) {
            model.addAttribute("myQueueCount", 0L);
        }
    }

    @GetMapping("/reports/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public String reportDetail(@AuthenticationPrincipal CustomUserDetails admin,
            @PathVariable Long id, Model model) {
        ReportResponse report = moderationService.getReport(id);

        var relatedReports = moderationService
                .getReportsByTarget(
                        com.buenbocao.api.common.enums.ReportTargetType.valueOf(report.getTargetType()),
                        report.getTargetId())
                .stream()
                .filter(r -> !r.getId().equals(report.getId()))
                .toList();

        User contentAuthor = null;
        com.buenbocao.api.recipe.entity.Recipe targetRecipe = null;

        if ("RECIPE".equals(report.getTargetType())) {
            targetRecipe = recipeRepository.findById(report.getTargetId()).orElse(null);
            if (targetRecipe != null) {
                contentAuthor = targetRecipe.getAuthor();
                // Forzar init del autor
                contentAuthor.getUsername();
                // ✅ NUEVO: inicializar todas las colecciones lazy DENTRO de la transacción
                org.hibernate.Hibernate.initialize(targetRecipe.getIngredients());
                org.hibernate.Hibernate.initialize(targetRecipe.getSteps());
                org.hibernate.Hibernate.initialize(targetRecipe.getMedia());
                org.hibernate.Hibernate.initialize(targetRecipe.getTags());
                org.hibernate.Hibernate.initialize(targetRecipe.getDiets());
                // Inicializar nested lazy (ingredient.ingredient)
                if (targetRecipe.getIngredients() != null) {
                    targetRecipe.getIngredients().forEach(ing -> {
                        if (ing.getIngredient() != null)
                            ing.getIngredient().getName();
                    });
                }
            }
        } else if ("USER".equals(report.getTargetType())) {
            contentAuthor = userRepository.findById(report.getTargetId()).orElse(null);
        }

        model.addAttribute("adminUsername", admin.getUsername());
        model.addAttribute("report", report);
        model.addAttribute("relatedReports", relatedReports);
        model.addAttribute("contentAuthor", contentAuthor);
        model.addAttribute("targetRecipe", targetRecipe);
        model.addAttribute("statuses", ReportStatus.values());
        model.addAttribute("activePage", "reports");
        return "admin/report-detail";
    }

    @PostMapping("/reports/{id}/reviewing")
    @PreAuthorize("hasRole('ADMIN')")
    public String markReviewing(@AuthenticationPrincipal CustomUserDetails admin,
            @PathVariable Long id, RedirectAttributes ra) {
        try {
            moderationService.markAsReviewing(admin, id);
            ra.addFlashAttribute("success", "Reporte marcado como 'En revisión'.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/adminControlPanel/reports/" + id;
    }

    @PostMapping("/reports/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public String resolveReport(@AuthenticationPrincipal CustomUserDetails admin,
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String notes,
            RedirectAttributes ra) {
        try {
            ResolveReportRequest req = new ResolveReportRequest();
            req.setStatus(ReportStatus.valueOf(status));
            req.setModeratorNotes(notes);
            moderationService.resolveReport(admin, id, req);
            ra.addFlashAttribute("success", "Reporte actualizado correctamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/adminControlPanel/reports/" + id;
    }

    @PostMapping("/reports/{id}/delete-content")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteContent(@AuthenticationPrincipal CustomUserDetails admin,
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            RedirectAttributes ra) {
        try {
            moderationService.deleteReportedContent(admin, id, reason);
            ra.addFlashAttribute("success", "Contenido archivado/eliminado y reporte resuelto.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/adminControlPanel/reports/" + id;
    }

    @PostMapping("/reports/{id}/lock-recipe")
    @PreAuthorize("hasRole('ADMIN')")
    public String lockRecipeFromReport(@AuthenticationPrincipal CustomUserDetails admin,
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            RedirectAttributes ra) {
        try {
            ReportResponse report = moderationService.getReport(id);
            if (report.getTargetType() == null || !report.getTargetType().equals("RECIPE")) {
                throw new RuntimeException("Este reporte no es sobre una receta");
            }
            recipeService.adminLockRecipe(admin, report.getTargetId(), reason);
            ra.addFlashAttribute("success", "Receta bloqueada y archivada por moderación.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/adminControlPanel/reports/" + id;
    }

    @PostMapping("/reports/{id}/unban-user")
    @PreAuthorize("hasRole('ADMIN')")
    public String unbanUserFromReport(@AuthenticationPrincipal CustomUserDetails admin,
            @PathVariable Long id, RedirectAttributes ra) {
        try {
            ReportResponse report = moderationService.getReport(id);
            Long targetUserId = null;
            if ("USER".equals(report.getTargetType())) {
                targetUserId = report.getTargetId();
            } else if ("RECIPE".equals(report.getTargetType())) {
                targetUserId = recipeRepository.findById(report.getTargetId())
                        .map(r -> r.getAuthor().getId()).orElse(null);
            }
            if (targetUserId == null)
                throw new RuntimeException("No se pudo identificar el usuario");
            moderationService.unbanUser(admin, targetUserId);
            ra.addFlashAttribute("success", "Usuario desbaneado correctamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/adminControlPanel/reports/" + id;
    }

    // Desbloquear receta directamente desde el detalle del reporte
    @PostMapping("/reports/{id}/unlock-recipe")
    @PreAuthorize("hasRole('ADMIN')")
    public String unlockRecipeFromReport(@AuthenticationPrincipal CustomUserDetails admin,
            @PathVariable Long id, RedirectAttributes ra) {
        try {
            ReportResponse report = moderationService.getReport(id);
            if (!"RECIPE".equals(report.getTargetType()))
                throw new RuntimeException("Este reporte no es de una receta");
            recipeService.changeStatus(admin, report.getTargetId(), RecipeStatus.PUBLISHED);
            ra.addFlashAttribute("success", "Receta desbloqueada y publicada.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/adminControlPanel/reports/" + id;
    }

    @PostMapping("/reports/{id}/ban-user")
    @PreAuthorize("hasRole('ADMIN')")
    public String banUserFromReport(@AuthenticationPrincipal CustomUserDetails admin,
            @PathVariable Long id,
            @RequestParam(required = false) String banReason,
            @RequestParam(required = false) Integer durationDays,
            RedirectAttributes ra) {
        try {
            moderationService.banUserFromReport(admin, id, banReason, durationDays);
            ra.addFlashAttribute("success", "Usuario baneado y reporte resuelto.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/adminControlPanel/reports/" + id;
    }

    // ════════════════════════════════════════════════
    // USUARIOS
    // ════════════════════════════════════════════════

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public String users(@AuthenticationPrincipal CustomUserDetails admin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String filter,
            Model model) {
        boolean isBanned = "banned".equals(filter);
        int size = 20;

        model.addAttribute("adminUsername", admin.getUsername());
        model.addAttribute("currentPage", page);
        model.addAttribute("search", search);
        model.addAttribute("filter", filter);
        model.addAttribute("isBannedFilter", isBanned);
        model.addAttribute("activePage", isBanned ? "banned" : "users");

        if (isBanned) {
            model.addAttribute("users", userRepository.findByEnabledFalse());
        } else if (search != null && !search.isBlank()) {
            model.addAttribute("usersPage", userRepository.searchByUsernameOrName(search, PageRequest.of(page, size)));
        } else {
            model.addAttribute("usersPage", userRepository.findAll(
                    PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"))));
        }
        return "admin/users";
    }

    @PostMapping("/users/{userId}/ban")
    @PreAuthorize("hasRole('ADMIN')")
    public String banUser(@PathVariable Long userId,
            @RequestParam(required = false) String banReason,
            @RequestParam(required = false) Integer durationDays,
            RedirectAttributes ra) {
        try {
            User user = userRepository.findById(userId).orElseThrow();
            if (!user.isEnabled())
                throw new RuntimeException("El usuario ya está baneado");
            user.setEnabled(false);
            user.setBanReason(banReason != null && !banReason.isBlank() ? banReason : "Violación de las normas");
            user.setBanExpiresAt(durationDays != null ? LocalDateTime.now().plusDays(durationDays) : null);
            userRepository.save(user);
            ra.addFlashAttribute("success", "Usuario baneado correctamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/adminControlPanel/users";
    }

    @PostMapping("/users/{userId}/unban")
    @PreAuthorize("hasRole('ADMIN')")
    public String unbanUser(@AuthenticationPrincipal CustomUserDetails admin,
            @PathVariable Long userId, RedirectAttributes ra) {
        try {
            moderationService.unbanUser(admin, userId);
            ra.addFlashAttribute("success", "Usuario desbaneado.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/adminControlPanel/users";
    }

    // ════════════════════════════════════════════════
    // RECETAS
    // ════════════════════════════════════════════════

    @GetMapping("/recipes")
    @PreAuthorize("hasRole('ADMIN')")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public String recipes(@AuthenticationPrincipal CustomUserDetails admin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            Model model) {
        int size = 20;
        PageRequest pr = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Recipe> recipesPage;
        if (search != null && !search.isBlank()) {
            recipesPage = recipeRepository.searchAllByTitleOrDescription(search, pr);
        } else if (status != null && !status.isBlank()) {
            recipesPage = recipeRepository.findByStatus(RecipeStatus.valueOf(status), pr);
        } else {
            recipesPage = recipeRepository.findAll(pr);
        }

        recipesPage.getContent().forEach(r -> {
            if (r.getAuthor() != null)
                r.getAuthor().getUsername();
        });

        model.addAttribute("adminUsername", admin.getUsername());
        model.addAttribute("recipesPage", recipesPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("search", search);
        model.addAttribute("filterStatus", status);
        model.addAttribute("statuses", RecipeStatus.values());
        model.addAttribute("activePage", "recipes");
        return "admin/recipes";
    }

    // Archivar receta con bloqueo admin (usuario no puede volver a publicarla)
    @PostMapping("/recipes/{id}/archive")
    @PreAuthorize("hasRole('ADMIN')")
    public String archiveRecipe(@AuthenticationPrincipal CustomUserDetails admin,
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            RedirectAttributes ra) {
        try {
            recipeService.adminLockRecipe(admin, id, reason);
            ra.addFlashAttribute("success", "Receta archivada y bloqueada. El usuario no podrá republicarla.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/adminControlPanel/recipes";
    }

    // Desbloquear receta (el admin la devuelve a publicada)
    @PostMapping("/recipes/{id}/unlock")
    @PreAuthorize("hasRole('ADMIN')")
    public String unlockRecipe(@AuthenticationPrincipal CustomUserDetails admin,
            @PathVariable Long id, RedirectAttributes ra) {
        try {
            recipeService.changeStatus(admin, id, RecipeStatus.PUBLISHED);
            ra.addFlashAttribute("success", "Receta desbloqueada y publicada de nuevo.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/adminControlPanel/recipes";
    }

    // ════════════════════════════════════════════════
    // EVENTOS
    // ════════════════════════════════════════════════

    @GetMapping("/events")
    @PreAuthorize("hasRole('ADMIN')")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public String events(@AuthenticationPrincipal CustomUserDetails admin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            Model model) {
        PageRequest pr = PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, "startsAt"));

        boolean hasStatus = status != null && !status.isBlank();
        boolean hasType = type != null && !type.isBlank();

        // Usar EventService para obtener DTOs ya mapeados (evita
        // LazyInitializationException)
        com.buenbocao.api.common.response.PagedResponse<com.buenbocao.api.event.dto.response.EventResponse> eventsPage;

        if (hasStatus && hasType) {
            eventsPage = eventService.getEventsByStatusAndType(
                    EventStatus.valueOf(status), EventType.valueOf(type), admin.getId(), page, 20);
        } else if (hasStatus) {
            eventsPage = eventService.getEventsByStatus(EventStatus.valueOf(status), admin.getId(), page, 20);
        } else if (hasType) {
            eventsPage = eventService.getEventsByType(EventType.valueOf(type), admin.getId(), page, 20);
        } else {
            eventsPage = eventService.getAllEventsAdmin(admin.getId(), page, 20);
        }

        long totalUpcoming = eventRepository.countByStatus(EventStatus.UPCOMING);
        long totalActive = eventRepository.countByStatus(EventStatus.ACTIVE);
        long totalDraft = eventRepository.countByStatus(EventStatus.DRAFT);
        long totalFinished = eventRepository.countByStatus(EventStatus.FINISHED);
        long totalCancelled = eventRepository.countByStatus(EventStatus.CANCELLED);

        model.addAttribute("adminUsername", admin.getUsername());
        model.addAttribute("eventsPage", eventsPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("filterStatus", status);
        model.addAttribute("filterType", type);
        model.addAttribute("statuses", EventStatus.values());
        model.addAttribute("eventTypes", EventType.values());
        model.addAttribute("totalUpcoming", totalUpcoming);
        model.addAttribute("totalActive", totalActive);
        model.addAttribute("totalDraft", totalDraft);
        model.addAttribute("totalFinished", totalFinished);
        model.addAttribute("totalCancelled", totalCancelled);
        model.addAttribute("activePage", "events");
        return "admin/events";
    }

    // ── Formulario nuevo evento ──

    @GetMapping("/events/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String newEventForm(@AuthenticationPrincipal CustomUserDetails admin, Model model) {
        model.addAttribute("adminUsername", admin.getUsername());
        model.addAttribute("eventTypes", EventType.values());
        model.addAttribute("statuses", EventStatus.values());
        model.addAttribute("editMode", false);
        model.addAttribute("activePage", "events");
        return "admin/event-form";
    }

    @PostMapping("/events/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String createEvent(@AuthenticationPrincipal CustomUserDetails admin,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String type,
            @RequestParam String startsAt,
            @RequestParam(required = false) String endsAt,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String coverImage,
            @RequestParam(defaultValue = "true") boolean online,
            @RequestParam(required = false) Integer maxParticipants,
            @RequestParam(defaultValue = "false") boolean premiumOnly,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) String price,
            @RequestParam(required = false) String joinUrl,
            @RequestParam(required = false) Integer minParticipants,
            @RequestParam(required = false) String registrationDeadline,
            @RequestParam(required = false) String requirements,
            RedirectAttributes ra) {
        try {
            CreateEventRequest req = buildRequest(title, description, type, startsAt, endsAt,
                    location, coverImage, online, maxParticipants, premiumOnly, tags,
                    price, joinUrl, minParticipants, registrationDeadline, requirements);
            eventService.createEvent(admin, req);
            ra.addFlashAttribute("success", "Evento " + title + " creado correctamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/adminControlPanel/events";
    }

    // ── Formulario editar evento ──

    @GetMapping("/events/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public String editEventForm(@AuthenticationPrincipal CustomUserDetails admin,
            @PathVariable Long id, Model model) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
        if (event.getOrganizer() != null)
            event.getOrganizer().getUsername();

        model.addAttribute("adminUsername", admin.getUsername());
        model.addAttribute("event", event);
        model.addAttribute("eventTypes", EventType.values());
        model.addAttribute("statuses", EventStatus.values());
        model.addAttribute("editMode", true);
        model.addAttribute("activePage", "events");
        return "admin/event-form";
    }

    @PostMapping("/events/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateEvent(@PathVariable Long id,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String type,
            @RequestParam String startsAt,
            @RequestParam(required = false) String endsAt,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String coverImage,
            @RequestParam(defaultValue = "true") boolean online,
            @RequestParam(required = false) Integer maxParticipants,
            @RequestParam(defaultValue = "false") boolean premiumOnly,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) String price,
            @RequestParam(required = false) String joinUrl,
            @RequestParam(required = false) Integer minParticipants,
            @RequestParam(required = false) String registrationDeadline,
            @RequestParam(required = false) String requirements,
            RedirectAttributes ra) {
        try {
            CreateEventRequest req = buildRequest(title, description, type, startsAt, endsAt,
                    location, coverImage, online, maxParticipants, premiumOnly, tags,
                    price, joinUrl, minParticipants, registrationDeadline, requirements);
            eventService.updateEvent(id, req);
            ra.addFlashAttribute("success", "Evento actualizado correctamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/adminControlPanel/events";
    }

    // ── Cambiar estado ──

    @PostMapping("/events/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public String changeEventStatus(@PathVariable Long id,
            @RequestParam String status,
            RedirectAttributes ra) {
        try {
            eventService.changeEventStatus(id, EventStatus.valueOf(status));
            ra.addFlashAttribute("success", "Estado del evento actualizado.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/adminControlPanel/events";
    }

    // ── Eliminar ──

    @PostMapping("/events/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteEvent(@PathVariable Long id, RedirectAttributes ra) {
        try {
            eventService.deleteEvent(id);
            ra.addFlashAttribute("success", "Evento eliminado correctamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/adminControlPanel/events";
    }

    // ── Upload imagen de evento ──

    @PostMapping("/events/upload-image")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public com.buenbocao.api.common.response.ApiResponse<String> uploadEventImage(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return com.buenbocao.api.common.response.ApiResponse.error("El archivo está vacío");
            }
            String path = storageService.store(file, "events");
            return com.buenbocao.api.common.response.ApiResponse.ok("Imagen subida", path);
        } catch (Exception e) {
            return com.buenbocao.api.common.response.ApiResponse.error("Error al subir la imagen: " + e.getMessage());
        }
    }

    // ── Builder privado ──

    private CreateEventRequest buildRequest(String title, String description, String type,
            String startsAt, String endsAt, String location, String coverImage,
            boolean online, Integer maxParticipants, boolean premiumOnly, String tags,
            String price, String joinUrl, Integer minParticipants, String registrationDeadline,
            String requirements) {
        CreateEventRequest req = new CreateEventRequest();
        req.setTitle(title);
        req.setDescription(description);
        req.setType(EventType.valueOf(type));
        req.setStartsAt(parseDateTime(startsAt));
        req.setEndsAt(parseDateTime(endsAt));
        req.setLocation(location);
        req.setCoverImage(coverImage != null && !coverImage.isBlank() ? coverImage : null);
        req.setOnline(online);
        req.setPrice(price != null && !price.isBlank() ? new java.math.BigDecimal(price) : null);
        req.setJoinUrl(joinUrl != null && !joinUrl.isBlank() ? joinUrl : null);
        req.setMinParticipants(minParticipants);
        req.setRegistrationDeadline(parseDateTime(registrationDeadline));
        req.setRequirements(requirements != null && !requirements.isBlank() ? requirements : null);
        req.setMaxParticipants(maxParticipants);
        req.setPremiumOnly(premiumOnly);
        req.setTags(tags);
        return req;
    }

    /** Parsea fechas de input datetime-local (con o sin segundos). */
    private LocalDateTime parseDateTime(String s) {
        if (s == null || s.isBlank()) return null;
        // datetime-local envía "yyyy-MM-ddTHH:mm" (16 chars, sin segundos)
        String normalized = s.length() == 16 ? s + ":00" : s;
        return LocalDateTime.parse(normalized);
    }

    // ════════════════════════════════════════════════
    // SUSCRIPCIONES
    // ════════════════════════════════════════════════

    @GetMapping("/subscriptions")
    @PreAuthorize("hasRole('ADMIN')")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public String subscriptions(@AuthenticationPrincipal CustomUserDetails admin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String status,
            Model model) {
        PageRequest pr = PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Subscription> subsPage = (status != null && !status.isBlank())
                ? subscriptionRepository.findByStatus(SubscriptionStatus.valueOf(status), pr)
                : subscriptionRepository.findAll(pr);

        // Forzar init de relaciones lazy dentro de la transacción
        subsPage.getContent().forEach(s -> {
            if (s.getUser() != null) s.getUser().getUsername();
        });

        long activeCount = subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE);
        long expiredCount = subscriptionRepository.countByStatus(SubscriptionStatus.EXPIRED);
        long cancelledCount = subscriptionRepository.countByStatus(SubscriptionStatus.CANCELLED);

        model.addAttribute("adminUsername", admin.getUsername());
        model.addAttribute("subsPage", subsPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("filterStatus", status);
        model.addAttribute("subStatuses", SubscriptionStatus.values());
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("expiredCount", expiredCount);
        model.addAttribute("cancelledCount", cancelledCount);
        model.addAttribute("activePage", "subscriptions");
        return "admin/subscriptions";
    }

    @GetMapping("/subscriptions/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public String subscriptionDetail(@AuthenticationPrincipal CustomUserDetails admin,
            @PathVariable Long id, Model model) {
        Subscription sub = subscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Suscripción no encontrada"));

        // Forzar init lazy
        if (sub.getUser() != null) {
            sub.getUser().getUsername();
            sub.getUser().getEmail();
            sub.getUser().getFirstName();
            sub.getUser().getLastName();
        }

        // Historial completo del usuario (ya ordenado DESC por createdAt)
        List<Subscription> userHistory = subscriptionRepository
                .findByUserIdOrderByCreatedAtDesc(sub.getUser().getId());

        model.addAttribute("adminUsername", admin.getUsername());
        model.addAttribute("sub", sub);
        model.addAttribute("userHistory", userHistory);
        model.addAttribute("activePage", "subscriptions");
        return "admin/subscription-detail";
    }

    // ════════════════════════════════════════════════
    // HELPER
    // ════════════════════════════════════════════════

    private <E extends Enum<E>> E parse(String value, Class<E> enumClass) {
        if (value == null || value.isBlank())
            return null;
        try {
            return Enum.valueOf(enumClass, value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}