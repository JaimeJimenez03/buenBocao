package com.buenbocao.api.event.entity;

import com.buenbocao.api.common.audit.AuditableEntity;
import com.buenbocao.api.common.enums.EventStatus;
import com.buenbocao.api.common.enums.EventType;
import com.buenbocao.api.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Evento culinario de la plataforma.
 * Solo los admins pueden crear eventos.
 */
@Entity
@Table(name = "events")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(length = 100)
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private EventType type;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventStatus status = EventStatus.DRAFT;

    @Column(name = "cover_image")
    private String coverImage;

    /** Fecha y hora de inicio del evento. */
    @Column(name = "starts_at", nullable = false)
    private LocalDateTime startsAt;

    /** Fecha y hora de fin del evento. */
    @Column(name = "ends_at")
    private LocalDateTime endsAt;

    /** Ubicación (puede ser URL si es online o dirección si es presencial). */
    @Column(length = 300)
    private String location;

    /** Si es un evento online. */
    @Builder.Default
    @Column(name = "is_online", nullable = false)
    private boolean online = true;

    /** Máximo de participantes (null = sin límite). */
    @Column(name = "max_participants")
    private Integer maxParticipants;

    /** Solo para premium. */
    @Builder.Default
    @Column(name = "premium_only", nullable = false)
    private boolean premiumOnly = false;

    /**
     * Organizador del evento (admin). Renombrado para evitar conflicto con
     * AuditableEntity.createdBy
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    /** Participantes del evento. */
    @Builder.Default
    @ManyToMany
    @JoinTable(name = "event_participants", joinColumns = @JoinColumn(name = "event_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> participants = new HashSet<>();

    /** Tags del evento para búsqueda. */
    @Column(length = 300)
    private String tags;

    /** Precio de entrada (null = gratuito). */
    @Column(name = "price", precision = 8, scale = 2)
    private java.math.BigDecimal price;

    /** Enlace externo para unirse (Zoom, Meet, etc.). */
    @Column(name = "join_url", length = 500)
    private String joinUrl;

    /** Mínimo de participantes para que el evento se celebre. */
    @Column(name = "min_participants")
    private Integer minParticipants;

    /** Fecha límite de inscripción. */
    @Column(name = "registration_deadline")
    private LocalDateTime registrationDeadline;

    /** Requisitos previos o materiales necesarios. */
    @Column(name = "requirements", columnDefinition = "TEXT")
    private String requirements;

    public boolean isFull() {
        return maxParticipants != null && participants.size() >= maxParticipants;
    }
}