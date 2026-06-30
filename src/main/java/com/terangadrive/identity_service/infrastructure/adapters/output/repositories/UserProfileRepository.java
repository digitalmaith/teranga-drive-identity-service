package com.terangadrive.identity_service.infrastructure.adapters.output.repositories;

import com.terangadrive.identity_service.infrastructure.adapters.output.entities.UserProfileEntity;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfileEntity, UUID> {
    // Grace à JpaRepository, on a déjà les méthodes de base :
    // save(), findById(), deleteById(), findAll(), etc.
    boolean existsByPhoneNumber(String phoneNumber);
}
