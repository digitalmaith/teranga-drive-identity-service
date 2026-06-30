package com.terangadrive.identity_service.domain.services;

import com.terangadrive.identity_service.domain.models.User;
import com.terangadrive.identity_service.domain.models.UserProfile;
import com.terangadrive.identity_service.domain.ports.input.CreateUserProfileUseCase;
import com.terangadrive.identity_service.domain.ports.output.UserOutputPort;
import com.terangadrive.identity_service.domain.ports.output.UserProfileOutputPort;
import com.terangadrive.identity_service.infrastructure.adapters.input.dtos.AuthResponse;
import com.terangadrive.identity_service.infrastructure.adapters.input.dtos.UserProfileResponse;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserProfileService implements CreateUserProfileUseCase {

    private final UserProfileOutputPort userProfileOutputPort;
    private final UserOutputPort userOutputPort;
    private final EntityManager entityManager;
    private final OtpService otpService;
    private final JwtService jwtService;

    public UserProfileService(
            UserProfileOutputPort userProfileOutputPort,
            UserOutputPort userOutputPort,
            EntityManager entityManager,
            OtpService otpService,
            JwtService jwtService) {
        this.userProfileOutputPort = userProfileOutputPort;
        this.userOutputPort = userOutputPort;
        this.entityManager = entityManager;
        this.otpService = otpService;
        this.jwtService = jwtService;
    }

    @Override
    public UserProfile execute(UserProfile userProfile) {
        if (userOutputPort.existsByEmail(userProfile.getEmail())) {
            throw new IllegalArgumentException("Cet email est déjà associé à un compte");
        }

        if (userProfileOutputPort.existsByPhoneNumber(userProfile.getPhoneNumber())) {
            throw new IllegalArgumentException("Ce numéro de téléphone est déjà utilisé");
        }

        otpService.generateAndSend(userProfile.getEmail(), userProfile);

        return userProfile;
    }

    // Vérifier OTP, créer le user + profil, et retourner directement les tokens
    @Transactional
    public AuthResponse confirmRegistration(String email, String otp) {
        UserProfile pendingUser = otpService.verifyAndGetPending(email, otp);

        if (pendingUser == null) {
            throw new IllegalArgumentException("OTP invalide ou expiré");
        }

        UUID userId = UUID.randomUUID();

        User user = new User(userId, pendingUser.getEmail());
        userOutputPort.save(user);
        entityManager.flush();

        userOutputPort.verifyEmail(userId);

        pendingUser.setId(userId);
        pendingUser.setCreatedAt(LocalDateTime.now());
        UserProfile savedProfile = userProfileOutputPort.save(pendingUser);

        // Auto-login : générer les tokens directement
        String role = savedProfile.getRole() != null ? savedProfile.getRole().name() : "USER";
        String accessToken = jwtService.generateAccessToken(userId, email, role);
        String refreshToken = jwtService.generateRefreshToken(userId);

        UserProfileResponse profileResponse = new UserProfileResponse(
                savedProfile.getId(),
                savedProfile.getFirstName(),
                savedProfile.getLastName(),
                email,
                savedProfile.getPhoneNumber(),
                savedProfile.getRole(),
                savedProfile.getCreatedAt()
        );

        return new AuthResponse(accessToken, refreshToken, jwtService.getAccessTokenExpiration(), profileResponse);
    }
}