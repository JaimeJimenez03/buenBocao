package com.buenbocao.api.moderation.repository;

import com.buenbocao.api.common.enums.ReportReason;
import com.buenbocao.api.common.enums.ReportStatus;
import com.buenbocao.api.common.enums.ReportTargetType;
import com.buenbocao.api.moderation.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

        boolean existsByReporterIdAndTargetTypeAndTargetId(
                        Long reporterId, ReportTargetType targetType, Long targetId);

        java.util.Optional<Report> findByReporterIdAndTargetTypeAndTargetId(
                        Long reporterId, ReportTargetType targetType, Long targetId);

        Page<Report> findByStatusOrderByCreatedAtAsc(ReportStatus status, Pageable pageable);

        @Query("""
                        SELECT r FROM Report r
                        WHERE (:status IS NULL OR r.status = :status)
                          AND (:reason IS NULL OR :reason MEMBER OF r.reasons)
                          AND (:targetType IS NULL OR r.targetType = :targetType)
                        ORDER BY r.createdAt DESC
                        """)
        Page<Report> findWithFilters(
                        @Param("status") ReportStatus status,
                        @Param("reason") ReportReason reason,
                        @Param("targetType") ReportTargetType targetType,
                        Pageable pageable);

        long countByStatus(ReportStatus status);

        long countByTargetTypeAndTargetId(ReportTargetType targetType, Long targetId);

        Page<Report> findByReporterIdOrderByCreatedAtDesc(Long reporterId, Pageable pageable);

        /**
         * Todos los reportes sobre el mismo contenido, para contexto en panel admin.
         */
        List<Report> findByTargetTypeAndTargetIdOrderByCreatedAtDesc(
                        ReportTargetType targetType, Long targetId);

        List<Report> findByResolvedByIdAndStatusOrderByUpdatedAtAsc(
                        Long resolvedById, ReportStatus status);

        /** Elimina reportes resueltos/desestimados con mas de N dias de antiguedad. */
        void deleteByStatusInAndUpdatedAtBefore(
                        java.util.List<ReportStatus> statuses, java.time.LocalDateTime before);
}