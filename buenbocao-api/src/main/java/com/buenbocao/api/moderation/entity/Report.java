package com.buenbocao.api.moderation.entity;

import com.buenbocao.api.common.audit.AuditableEntity;
import com.buenbocao.api.common.enums.ReportReason;
import com.buenbocao.api.common.enums.ReportStatus;
import com.buenbocao.api.common.enums.ReportTargetType;
import com.buenbocao.api.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Reporte de contenido inapropiado.
 * Un usuario reporta una receta, review u otro usuario.
 * Puede incluir varios motivos a la vez.
 */
@Entity
@Table(name = "reports")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Report extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private ReportTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    /** Uno o varios motivos del reporte, almacenados en tabla auxiliar. */
    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "report_reasons", joinColumns = @JoinColumn(name = "report_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "reason", length = 30)
    private List<ReportReason> reasons = new ArrayList<>();

    @Column(length = 500)
    private String description;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportStatus status = ReportStatus.PENDING;

    /** Nota del moderador al resolver el reporte. */
    @Column(name = "moderator_notes", length = 500)
    private String moderatorNotes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by")
    private User resolvedBy;
}