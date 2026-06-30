package com.terangadrive.identity_service.domain.models;

import java.time.LocalDateTime;
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
    private boolean emailVerified;
    private String pinCode;
    private Integer pinAttempts;
    private LocalDateTime pinLockedUntil;

    public User(UUID id, String email) {
        this.id = id;
        this.email = email;
        this.emailVerified = false;
        this.pinAttempts = 0;
    }

    public User(UUID id, String email, boolean emailVerified) {
        this.id = id;
        this.email = email;
        this.emailVerified = emailVerified;
        this.pinAttempts = 0;
    }
}