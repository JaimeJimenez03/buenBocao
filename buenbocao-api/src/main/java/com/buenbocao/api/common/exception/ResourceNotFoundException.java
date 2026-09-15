package com.buenbocao.api.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando no se encuentra un recurso solicitado.
 * Ejemplo: buscar un usuario o receta por ID que no existe.
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(
                String.format("%s no encontrado con %s: '%s'", resourceName, fieldName, fieldValue),
                HttpStatus.NOT_FOUND
        );
    }

    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
