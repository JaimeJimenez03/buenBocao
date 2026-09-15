package com.buenbocao.api.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Wrapper estándar para TODAS las respuestas de la API.
 * Garantiza que el front siempre reciba el mismo formato.
 *
 * Ejemplo éxito:  { "success": true,  "message": "Receta creada", "data": {...}, "timestamp": "..." }
 * Ejemplo error:  { "success": false, "message": "No encontrado", "data": null,  "timestamp": "..." }
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final String message;
    private final T data;

    @Builder.Default
    private final LocalDateTime timestamp = LocalDateTime.now();

    // ---- Factory methods ----

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> ok(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> validationError(T errors) {
        return ApiResponse.<T>builder()
                .success(false)
                .message("Errores de validación")
                .data(errors)
                .build();
    }
}
