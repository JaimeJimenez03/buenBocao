package com.buenbocao.api.user.dto.request;

import com.buenbocao.api.common.constants.AppConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para actualizar los datos del perfil.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50, message = "El nombre no puede superar los {max} caracteres")
    private String firstName;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 50, message = "El apellido no puede superar los {max} caracteres")
    private String lastName;

    @Size(max = AppConstants.BIO_MAX_LENGTH,
          message = "La biografía no puede superar los {max} caracteres")
    private String bio;
}
