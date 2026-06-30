package com.terangadrive.identity_service.domain.models;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private UUID id;
    private String email;
    private String password;
    private boolean emailVerified;

    // Constructeur sans emailVerified pour la création
    public User(UUID id, String email, String password) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.emailVerified = false;
    }

}