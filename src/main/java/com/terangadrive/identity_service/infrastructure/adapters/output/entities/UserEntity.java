package com.terangadrive.identity_service.infrastructure.adapters.output.entities;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users") // Assure-toi que c'est le nom exact de ta table dans Supabase
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "email_verified" , nullable = false)
    private  boolean emailVerified = false;
}