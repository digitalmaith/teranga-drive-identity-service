package com.terangadrive.identity_service.infrastructure.adapters.output.entities;

import com.terangadrive.identity_service.domain.enums.UserRole;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;
import java.time.LocalDateTime;

@Entity
@Table(name = "profiles") // Fait le lien avec la table Supabase
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileEntity {

    @Id // Indique que c'est la clé primaire
    private UUID id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "phone_number", unique = true, nullable = false)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", columnDefinition = "user_role")
    private UserRole role;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}