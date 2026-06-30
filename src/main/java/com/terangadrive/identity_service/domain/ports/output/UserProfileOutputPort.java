package com.terangadrive.identity_service.domain.ports.output;

import com.terangadrive.identity_service.domain.models.UserProfile;
import java.util.Optional;
import java.util.UUID;

public interface UserProfileOutputPort {
    UserProfile save(UserProfile userProfile);
    Optional<UserProfile> findById(UUID id);
    boolean existsByPhoneNumber(String phoneNumber);
}
