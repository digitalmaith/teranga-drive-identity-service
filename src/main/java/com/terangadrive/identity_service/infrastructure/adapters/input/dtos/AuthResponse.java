package com.terangadrive.identity_service.infrastructure.adapters.input.dtos;

import com.terangadrive.identity_service.domain.models.UserProfile;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private String tokenType = "Bearer";
    private UserProfileResponse profile;

    public AuthResponse(String accessToken, String refreshToken, long expiresIn, UserProfileResponse profile) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.profile = profile;
    }
}