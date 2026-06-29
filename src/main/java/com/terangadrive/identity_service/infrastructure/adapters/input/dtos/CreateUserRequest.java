package com.terangadrive.identity_service.infrastructure.adapters.input.dtos;

import com.terangadrive.identity_service.domain.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreateUserRequest {

    @NotBlank(message = "Le prénom est obligatoire")
    private String firstName;

    @NotBlank(message = "Le nom est obligatoire")
    private String lastName;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format email invalide")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    private String password;

    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    @Pattern(
            regexp = "^\\+221(77|76|75|78|70|71)[0-9]{7}$",
            message = "Numéro de téléphone invalide (format Sénégal attendu)"
    )
    private String phoneNumber;

    private UserRole role;
}