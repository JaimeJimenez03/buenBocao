package com.buenbocao.api.event.repository;

import com.buenbocao.api.common.enums.EventStatus;
import com.buenbocao.api.common.enums.EventType;
import com.buenbocao.api.event.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    Optional<Event> findBySlug(String slug);

    Page<Event> findByStatusOrderByStartsAtAsc(EventStatus status, Pageable pageable);

    Page<Event> findByStatusInOrderByStartsAtAsc(List<EventStatus> statuses, Pageable pageable);

    Page<Event> findByTypeOrderByStartsAtDesc(EventType type, Pageable pageable);

    Page<Event> findByStatusAndTypeOrderByStartsAtDesc(EventStatus status, EventType type, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.startsAt >= :now AND e.status IN ('UPCOMING', 'ACTIVE') ORDER BY e.startsAt ASC")
    Page<Event> findUpcomingEvents(@Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT e FROM Event e JOIN e.participants p WHERE p.id = :userId ORDER BY e.startsAt DESC")
    Page<Event> findByParticipantId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COUNT(p) FROM Event e JOIN e.participants p WHERE e.id = :eventId")
    long countParticipants(@Param("eventId") Long eventId);

    // ── Añadido para el panel admin ──
    long countByStatus(EventStatus status);
}