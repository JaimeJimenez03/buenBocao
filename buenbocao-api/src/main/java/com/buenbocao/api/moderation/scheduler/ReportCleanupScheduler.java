package com.buenbocao.api.moderation.scheduler;

import com.buenbocao.api.common.enums.ReportStatus;
import com.buenbocao.api.moderation.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Limpia automaticamente los reportes resueltos o desestimados
 * que tengan mas de 15 dias de antiguedad.
 *
 * Se ejecuta cada dia a las 3:00 AM para no interferir con el uso normal.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReportCleanupScheduler {

    // ⚠ SOLO PARA PRUEBAS — cambiar a 15 en produccion
    private static final int RETENTION_DAYS = 15;

    private final ReportRepository reportRepository;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanOldResolvedReports() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(RETENTION_DAYS);

        List<ReportStatus> statusesToDelete = List.of(
                ReportStatus.RESOLVED,
                ReportStatus.DISMISSED);

        reportRepository.deleteByStatusInAndUpdatedAtBefore(statusesToDelete, cutoff);

        log.info("Limpieza de reportes: eliminados reportes RESOLVED/DISMISSED con mas de {} dias (anteriores a {})",
                RETENTION_DAYS, cutoff);
    }
}