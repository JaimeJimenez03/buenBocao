package com.buenbocao.api.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando se intenta crear un recurso que ya existe.
 * Ejemplo: registrar un email que ya está en uso.
 */
public class DuplicateResourceException extends BusinessException {

    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super(
                String.format("%s ya existe con %s: '%s'", resourceName, fieldName, fieldValue),
                HttpStatus.CONFLICT
        );
    }
}
