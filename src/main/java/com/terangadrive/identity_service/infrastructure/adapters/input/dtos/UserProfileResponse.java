package com.terangadrive.identity_service.infrastructure.adapters.input.dtos;

import com.terangadrive.identity_service.domain.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class UserProfileResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private UserRole role;
    private LocalDateTime createdAt;
}