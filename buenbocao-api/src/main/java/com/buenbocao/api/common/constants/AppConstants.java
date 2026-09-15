package com.buenbocao.api.common.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Constantes globales de la aplicación.
 * Clase no instanciable — solo contiene constantes estáticas.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AppConstants {

    // Paginación
    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_PAGE_SIZE = "20";
    public static final int MAX_PAGE_SIZE = 50;
    public static final String DEFAULT_SORT_BY = "createdAt";
    public static final String DEFAULT_SORT_DIR = "desc";

    // Validaciones
    public static final int USERNAME_MIN_LENGTH = 4;
    public static final int USERNAME_MAX_LENGTH = 30;
    public static final int PASSWORD_MIN_LENGTH = 4;
    public static final int BIO_MAX_LENGTH = 300;
    public static final int RECIPE_TITLE_MAX_LENGTH = 150;
    public static final int RECIPE_DESCRIPTION_MAX_LENGTH = 2000;
    public static final int COMMENT_MAX_LENGTH = 500;

    // Roles
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    // Cooldowns
    public static final int USERNAME_CHANGE_COOLDOWN_DAYS = 7;
}
