package com.terangadrive.identity_service.domain.models;

import com.terangadrive.identity_service.domain.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {
    private UUID id;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private UserRole role;
    private LocalDateTime createdAt;

    // Ajoutez ces deux lignes pour l'inscription :
    private String email;
    private String password;
}