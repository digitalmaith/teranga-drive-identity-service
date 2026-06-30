package com.terangadrive.identity_service.domain.ports.output;

import com.terangadrive.identity_service.domain.models.User;

import java.util.Optional;
import java.util.UUID;

public interface UserOutputPort {
    User save(User user);
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    Optional<User> findById(UUID id);
    void verifyEmail(UUID id);
}