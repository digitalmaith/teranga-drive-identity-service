package com.terangadrive.identity_service.infrastructure.adapters.output.persistence;

import com.terangadrive.identity_service.domain.models.UserProfile;
import com.terangadrive.identity_service.domain.ports.output.UserProfileOutputPort;
import com.terangadrive.identity_service.infrastructure.adapters.output.entities.UserProfileEntity;
import com.terangadrive.identity_service.infrastructure.adapters.output.repositories.UserProfileRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class UserProfilePersistenceAdapter implements UserProfileOutputPort {

    private final UserProfileRepository repository;
    private final EntityManager entityManager;

    public UserProfilePersistenceAdapter(UserProfileRepository repository, EntityManager entityManager) {
        this.repository = repository;
        this.entityManager = entityManager;
    }

    @Override
    public UserProfile save(UserProfile userProfile) {
        UserProfileEntity entity = new UserProfileEntity(
                userProfile.getId(),
                userProfile.getFirstName(),
                userProfile.getLastName(),
                userProfile.getPhoneNumber(),
                userProfile.getRole(),
                userProfile.getCreatedAt()
        );
        entityManager.persist(entity);

        UserProfile result = new UserProfile();
        result.setId(entity.getId());
        result.setFirstName(entity.getFirstName());
        result.setLastName(entity.getLastName());
        result.setPhoneNumber(entity.getPhoneNumber());
        result.setRole(entity.getRole());
        result.setCreatedAt(entity.getCreatedAt());
        result.setEmail(userProfile.getEmail());
        result.setPassword(userProfile.getPassword());
        return result;
    }

    @Override
    public Optional<UserProfile> findById(UUID id) {
        return repository.findById(id)
                .map(entity -> {
                    UserProfile profile = new UserProfile();
                    profile.setId(entity.getId());
                    profile.setFirstName(entity.getFirstName());
                    profile.setLastName(entity.getLastName());
                    profile.setPhoneNumber(entity.getPhoneNumber());
                    profile.setRole(entity.getRole());
                    profile.setCreatedAt(entity.getCreatedAt());
                    return profile;
                });
    }
}