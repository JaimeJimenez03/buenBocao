package com.buenbocao.api.user.dto.request;

import com.buenbocao.api.common.constants.AppConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para cambiar el nombre de usuario.
 * El cambio tiene un período de cooldown configurable.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangeUsernameRequest {

    @NotBlank(message = "El nuevo nombre de usuario es obligatorio")
    @Size(min = AppConstants.USERNAME_MIN_LENGTH, max = AppConstants.USERNAME_MAX_LENGTH, message = "El nombre de usuario debe tener entre {min} y {max} caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9._]+$", message = "El nombre de usuario solo puede contener letras, números, puntos y guiones bajos")
    private String newUsername;

    @NotBlank(message = "La contraseña es obligatoria para confirmar el cambio")
    private String password;
}
