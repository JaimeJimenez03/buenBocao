package com.buenbocao.api.event.service;

import com.buenbocao.api.common.constants.AppConstants;
import com.buenbocao.api.common.enums.EventStatus;
import com.buenbocao.api.common.enums.EventType;
import com.buenbocao.api.common.exception.BusinessException;
import com.buenbocao.api.common.exception.ResourceNotFoundException;
import com.buenbocao.api.common.response.PagedResponse;
import com.buenbocao.api.common.util.SlugUtils;
import com.buenbocao.api.event.dto.request.CreateEventRequest;
import com.buenbocao.api.event.dto.response.EventResponse;
import com.buenbocao.api.event.entity.Event;
import com.buenbocao.api.event.repository.EventRepository;
import com.buenbocao.api.security.CustomUserDetails;
import com.buenbocao.api.user.entity.User;
import com.buenbocao.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    // ==================== ADMIN ====================

    @Transactional
    public EventResponse createEvent(CustomUserDetails admin, CreateEventRequest request) {
        User creator = userRepository.findById(admin.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", admin.getId()));

        String slug = SlugUtils.toSlug(request.getTitle());
        if (eventRepository.findBySlug(slug).isPresent()) {
            slug = slug + "-" + UUID.randomUUID().toString().substring(0, 6);
        }

        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .slug(slug)
                .type(request.getType())
                .status(EventStatus.UPCOMING)
                .coverImage(request.getCoverImage())
                .startsAt(request.getStartsAt())
                .endsAt(request.getEndsAt())
                .location(request.getLocation())
                .online(request.isOnline())
                .maxParticipants(request.getMaxParticipants())
                .premiumOnly(request.isPremiumOnly())
                .organizer(creator)
                .tags(request.getTags())
                .price(request.getPrice())
                .joinUrl(request.getJoinUrl())
                .minParticipants(request.getMinParticipants())
                .registrationDeadline(request.getRegistrationDeadline())
                .requirements(request.getRequirements())
                .build();

        Event saved = eventRepository.save(event);
        log.info("Evento creado: '{}' por admin {}", saved.getTitle(), admin.getUsername());

        return toResponse(saved, null);
    }

    @Transactional
    public EventResponse updateEvent(Long eventId, CreateEventRequest request) {
        Event event = findEventById(eventId);
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setType(request.getType());
        event.setCoverImage(request.getCoverImage());
        event.setStartsAt(request.getStartsAt());
        event.setEndsAt(request.getEndsAt());
        event.setLocation(request.getLocation());
        event.setOnline(request.isOnline());
        event.setMaxParticipants(request.getMaxParticipants());
        event.setPremiumOnly(request.isPremiumOnly());
        event.setTags(request.getTags());
        event.setPrice(request.getPrice());
        event.setJoinUrl(request.getJoinUrl());
        event.setMinParticipants(request.getMinParticipants());
        event.setRegistrationDeadline(request.getRegistrationDeadline());
        event.setRequirements(request.getRequirements());

        Event updated = eventRepository.save(event);
        return toResponse(updated, null);
    }

    @Transactional
    public EventResponse changeEventStatus(Long eventId, EventStatus status) {
        Event event = findEventById(eventId);
        event.setStatus(status);
        Event updated = eventRepository.save(event);
        return toResponse(updated, null);
    }

    @Transactional
    public void deleteEvent(Long eventId) {
        Event event = findEventById(eventId);
        eventRepository.delete(event);
    }

    // ==================== ADMIN LISTADO ====================

    @Transactional(readOnly = true)
    public PagedResponse<EventResponse> getAllEventsAdmin(Long currentUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startsAt"));
        Page<Event> eventsPage = eventRepository.findAll(pageable);
        List<EventResponse> content = eventsPage.getContent().stream()
                .map(e -> toResponse(e, currentUserId)).toList();
        return PagedResponse.of(eventsPage, content);
    }

    @Transactional(readOnly = true)
    public PagedResponse<EventResponse> getEventsByStatus(EventStatus status, Long currentUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Event> eventsPage = eventRepository.findByStatusOrderByStartsAtAsc(status, pageable);
        List<EventResponse> content = eventsPage.getContent().stream()
                .map(e -> toResponse(e, currentUserId)).toList();
        return PagedResponse.of(eventsPage, content);
    }

    @Transactional(readOnly = true)
    public PagedResponse<EventResponse> getEventsByType(EventType type, Long currentUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Event> eventsPage = eventRepository.findByTypeOrderByStartsAtDesc(type, pageable);
        List<EventResponse> content = eventsPage.getContent().stream()
                .map(e -> toResponse(e, currentUserId)).toList();
        return PagedResponse.of(eventsPage, content);
    }

    @Transactional(readOnly = true)
    public PagedResponse<EventResponse> getEventsByStatusAndType(EventStatus status, EventType type,
            Long currentUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Event> eventsPage = eventRepository.findByStatusAndTypeOrderByStartsAtDesc(status, type, pageable);
        List<EventResponse> content = eventsPage.getContent().stream()
                .map(e -> toResponse(e, currentUserId)).toList();
        return PagedResponse.of(eventsPage, content);
    }

    // ==================== PÚBLICO ====================

    @Transactional(readOnly = true)
    public EventResponse getEvent(Long eventId, Long currentUserId) {
        Event event = findEventById(eventId);
        return toResponse(event, currentUserId);
    }

    @Transactional(readOnly = true)
    public EventResponse getEventBySlug(String slug, Long currentUserId) {
        Event event = eventRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Evento", "slug", slug));
        return toResponse(event, currentUserId);
    }

    @Transactional(readOnly = true)
    public PagedResponse<EventResponse> getUpcomingEvents(Long currentUserId, int page, int size) {
        size = Math.min(size, AppConstants.MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, size);
        Page<Event> eventsPage = eventRepository.findUpcomingEvents(LocalDateTime.now(), pageable);
        List<EventResponse> content = eventsPage.getContent().stream()
                .map(e -> toResponse(e, currentUserId)).toList();
        return PagedResponse.of(eventsPage, content);
    }

    @Transactional(readOnly = true)
    public PagedResponse<EventResponse> getMyEvents(Long userId, int page, int size) {
        size = Math.min(size, AppConstants.MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, size);
        Page<Event> eventsPage = eventRepository.findByParticipantId(userId, pageable);
        List<EventResponse> content = eventsPage.getContent().stream()
                .map(e -> toResponse(e, userId)).toList();
        return PagedResponse.of(eventsPage, content);
    }

    // ==================== PARTICIPACIÓN ====================

    @Transactional
    public EventResponse joinEvent(CustomUserDetails currentUser, Long eventId) {
        Event event = findEventById(eventId);
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", currentUser.getId()));

        if (event.getStatus() != EventStatus.UPCOMING && event.getStatus() != EventStatus.ACTIVE) {
            throw new BusinessException("Este evento no acepta inscripciones");
        }
        if (event.isFull()) {
            throw new BusinessException("El evento está completo");
        }
        if (event.getParticipants().contains(user)) {
            throw new BusinessException("Ya estás inscrito en este evento");
        }

        event.getParticipants().add(user);
        Event updated = eventRepository.save(event);
        log.info("Usuario {} inscrito en evento '{}'", currentUser.getUsername(), event.getTitle());

        return toResponse(updated, currentUser.getId());
    }

    @Transactional
    public EventResponse leaveEvent(CustomUserDetails currentUser, Long eventId) {
        Event event = findEventById(eventId);
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", currentUser.getId()));

        if (!event.getParticipants().remove(user)) {
            throw new BusinessException("No estás inscrito en este evento");
        }

        Event updated = eventRepository.save(event);
        return toResponse(updated, currentUser.getId());
    }

    // ==================== PRIVADO ====================

    private Event findEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento", "id", id));
    }

    private EventResponse toResponse(Event event, Long currentUserId) {
        boolean joined = currentUserId != null && event.getParticipants().stream()
                .anyMatch(p -> p.getId().equals(currentUserId));

        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .slug(event.getSlug())
                .type(event.getType().name())
                .status(event.getStatus().name())
                .coverImage(event.getCoverImage())
                .startsAt(event.getStartsAt())
                .endsAt(event.getEndsAt())
                .location(event.getLocation())
                .online(event.isOnline())
                .maxParticipants(event.getMaxParticipants())
                .currentParticipants(event.getParticipants().size())
                .premiumOnly(event.isPremiumOnly())
                .full(event.isFull())
                .joinedByCurrentUser(joined)
                .organizerUsername(event.getOrganizer().getUsername())
                .tags(event.getTags())
                .price(event.getPrice())
                .free(event.getPrice() == null || event.getPrice().compareTo(java.math.BigDecimal.ZERO) == 0)
                .joinUrl(event.getJoinUrl())
                .minParticipants(event.getMinParticipants())
                .registrationDeadline(event.getRegistrationDeadline())
                .requirements(event.getRequirements())
                .createdAt(event.getCreatedAt())
                .build();
    }
}