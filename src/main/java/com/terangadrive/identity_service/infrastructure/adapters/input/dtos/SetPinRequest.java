// SetPinRequest.java
package com.terangadrive.identity_service.infrastructure.adapters.input.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SetPinRequest {

    @NotBlank(message = "Le PIN est obligatoire")
    @Pattern(regexp = "^[0-9]{4}$", message = "Le PIN doit contenir exactement 4 chiffres")
    private String pin;
}