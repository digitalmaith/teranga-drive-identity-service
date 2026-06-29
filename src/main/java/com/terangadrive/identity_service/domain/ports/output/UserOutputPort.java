package com.terangadrive.identity_service.domain.ports.output;

import com.terangadrive.identity_service.domain.models.User;

import java.util.Optional;

public interface UserOutputPort {
    User save(User user);
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
}