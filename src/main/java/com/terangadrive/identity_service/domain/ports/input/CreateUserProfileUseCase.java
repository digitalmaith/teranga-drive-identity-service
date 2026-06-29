package com.terangadrive.identity_service.domain.ports.input;

import com.terangadrive.identity_service.domain.models.UserProfile;

public interface CreateUserProfileUseCase {
    UserProfile execute(UserProfile userProfile);
}
