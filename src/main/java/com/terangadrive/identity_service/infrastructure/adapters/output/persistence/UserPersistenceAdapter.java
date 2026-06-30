package com.terangadrive.identity_service.infrastructure.adapters.output.persistence;

import com.terangadrive.identity_service.domain.models.User;
import com.terangadrive.identity_service.domain.ports.output.UserOutputPort;
import com.terangadrive.identity_service.infrastructure.adapters.output.entities.UserEntity;
import com.terangadrive.identity_service.infrastructure.adapters.output.repositories.UserRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
public class UserPersistenceAdapter implements UserOutputPort {

    private final UserRepository userRepository;
    private final EntityManager entityManager;

    public UserPersistenceAdapter(UserRepository userRepository, EntityManager entityManager) {
        this.userRepository = userRepository;
        this.entityManager = entityManager;
    }

    @Override
    public User save(User user) {
        UserEntity entity = new UserEntity(
                user.getId(),
                user.getEmail(),
                user.isEmailVerified(),
                user.getPinCode(),
                user.getPinAttempts(),
                user.getPinLockedUntil()
        );
        entityManager.persist(entity);
        return toDomain(entity);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id).map(this::toDomain);
    }

    @Override
    public void verifyEmail(UUID userId) {
        userRepository.findById(userId).ifPresent(entity -> {
            entity.setEmailVerified(true);
            userRepository.save(entity);
        });
    }

    @Override
    public void updatePin(UUID userId, String hashedPin) {
        userRepository.findById(userId).ifPresent(entity -> {
            entity.setPinCode(hashedPin);
            entity.setPinAttempts(0);
            entity.setPinLockedUntil(null);
            userRepository.save(entity);
        });
    }

    @Override
    public void incrementPinAttempts(UUID userId) {
        userRepository.findById(userId).ifPresent(entity -> {
            int attempts = entity.getPinAttempts() == null ? 0 : entity.getPinAttempts();
            entity.setPinAttempts(attempts + 1);
            userRepository.save(entity);
        });
    }

    @Override
    public void resetPinAttempts(UUID userId) {
        userRepository.findById(userId).ifPresent(entity -> {
            entity.setPinAttempts(0);
            entity.setPinLockedUntil(null);
            userRepository.save(entity);
        });
    }

    @Override
    public void lockPin(UUID userId, LocalDateTime lockedUntil) {
        userRepository.findById(userId).ifPresent(entity -> {
            entity.setPinLockedUntil(lockedUntil);
            userRepository.save(entity);
        });
    }

    private User toDomain(UserEntity entity) {
        return new User(
                entity.getId(),
                entity.getEmail(),
                entity.isEmailVerified(),
                entity.getPinCode(),
                entity.getPinAttempts(),
                entity.getPinLockedUntil()
        );
    }
}