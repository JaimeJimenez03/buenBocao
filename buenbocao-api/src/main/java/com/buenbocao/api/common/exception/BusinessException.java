package com.buenbocao.api.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Excepción base para errores de lógica de negocio.
 * Todas las excepciones custom de la app extienden de esta.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final HttpStatus status;

    public BusinessException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public BusinessException(String message) {
        this(message, HttpStatus.BAD_REQUEST);
    }
}
