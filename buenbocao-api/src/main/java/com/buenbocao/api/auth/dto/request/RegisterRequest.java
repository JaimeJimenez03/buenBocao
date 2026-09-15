package com.buenbocao.api.auth.dto.request;

import com.buenbocao.api.common.constants.AppConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para la petición de registro de un nuevo usuario.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = AppConstants.USERNAME_MIN_LENGTH,
          max = AppConstants.USERNAME_MAX_LENGTH,
          message = "El nombre de usuario debe tener entre {min} y {max} caracteres")
    private String username;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = AppConstants.PASSWORD_MIN_LENGTH,
          message = "La contraseña debe tener al menos {min} caracteres")
    private String password;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50, message = "El nombre no puede superar los {max} caracteres")
    private String firstName;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 50, message = "El apellido no puede superar los {max} caracteres")
    private String lastName;
}
