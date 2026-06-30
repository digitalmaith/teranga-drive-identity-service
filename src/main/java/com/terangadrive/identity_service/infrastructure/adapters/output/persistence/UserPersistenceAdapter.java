package com.terangadrive.identity_service.infrastructure.adapters.output.persistence;

import com.terangadrive.identity_service.domain.models.User;
import com.terangadrive.identity_service.domain.ports.output.UserOutputPort;
import com.terangadrive.identity_service.infrastructure.adapters.output.entities.UserEntity;
import com.terangadrive.identity_service.infrastructure.adapters.output.repositories.UserRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

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
                user.getPassword()
        );
        entityManager.persist(entity);
        return new User(entity.getId(), entity.getEmail(), entity.getPassword());
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(entity -> new User(entity.getId(), entity.getEmail(), entity.getPassword()));
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id)
                .map(entity -> new User(entity.getId(), entity.getEmail(), entity.getPassword()));
    }
}