package com.buenbocao.api.common.enums;

/**
 * Tipos de notificación del sistema.
 */
public enum NotificationType {
    NEW_FOLLOWER, // Nuevo seguidor
    NEW_REVIEW, // Nueva reseña
    NEW_FAVORITE, // Guardado en favoritos
    NEW_LIKE, // Like en una receta
    RECIPE_PUBLISHED, // Receta publicada
    EVENT_REMINDER, // Recordatorio de evento
    EVENT_INVITATION, // Invitación a evento
    REPORT_RESOLVED, // Reporte resuelto
    REPORT_REVIEWING, // Reporte en revisión
    REPORT_DISMISSED, // Reporte desestimado
    SUBSCRIPTION_EXPIRING, // Suscripción por vencer
    SYSTEM // Sistema
}