package com.buenbocao.api.common.enums;

/**
 * Estados posibles de una receta.
 */
public enum RecipeStatus {

    DRAFT, // Borrador — solo visible para el autor
    PUBLISHED, // Publicada — visible en el feed
    ARCHIVED, // Archivada — oculta por el propio usuario
    LOCKED // Bloqueada por moderacion — el usuario no puede cambiar el estado
}