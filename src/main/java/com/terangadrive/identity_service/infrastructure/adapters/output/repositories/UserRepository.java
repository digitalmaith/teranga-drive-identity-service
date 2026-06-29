package com.terangadrive.identity_service.infrastructure.adapters.output.repositories;

import com.terangadrive.identity_service.infrastructure.adapters.output.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    boolean existsByEmail(String email);
    Optional<UserEntity> findByEmail(String email);
}