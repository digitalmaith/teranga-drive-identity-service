// LoginPinRequest.java
package com.terangadrive.identity_service.infrastructure.adapters.input.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class LoginPinRequest {

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format email invalide")
    private String email;

    @NotBlank(message = "Le PIN est obligatoire")
    @Pattern(regexp = "^[0-9]{4}$", message = "Le PIN doit contenir 4 chiffres")
    private String pin;
}