package com.buenbocao.api.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando un usuario intenta acceder a un recurso sin autorización.
 */
public class UnauthorizedException extends BusinessException {

    public UnauthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }

    public UnauthorizedException() {
        this("No tienes permisos para realizar esta acción");
    }
}
