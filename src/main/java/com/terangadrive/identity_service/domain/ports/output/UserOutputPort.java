package com.terangadrive.identity_service.domain.ports.output;

import com.terangadrive.identity_service.domain.models.User;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface UserOutputPort {
    User save(User user);
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    Optional<User> findById(UUID id);
    void verifyEmail(UUID userId);
    void updatePin(UUID userId, String hashedPin);
    void incrementPinAttempts(UUID userId);
    void resetPinAttempts(UUID userId);
    void lockPin(UUID userId, LocalDateTime lockedUntil);
}