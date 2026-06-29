// LoginUseCase.java
package com.terangadrive.identity_service.domain.ports.input;

import com.terangadrive.identity_service.infrastructure.adapters.input.dtos.AuthResponse;
import com.terangadrive.identity_service.infrastructure.adapters.input.dtos.LoginRequest;

public interface LoginUseCase {
    AuthResponse execute(LoginRequest request);
}