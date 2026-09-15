package com.buenbocao.api.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para cambiar el email.
 * Al cambiar el email, se requiere re-verificación.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangeEmailRequest {

    @NotBlank(message = "El nuevo email es obligatorio")
    @Email(message = "El formato del email no es válido")
    private String newEmail;

    @NotBlank(message = "La contraseña es obligatoria para confirmar el cambio")
    private String password;
}
