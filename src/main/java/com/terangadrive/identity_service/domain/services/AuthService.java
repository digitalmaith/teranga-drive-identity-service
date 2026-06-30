package com.terangadrive.identity_service.domain.services;

import com.terangadrive.identity_service.domain.models.User;
import com.terangadrive.identity_service.domain.ports.input.LoginUseCase;
import com.terangadrive.identity_service.domain.ports.output.UserOutputPort;
import com.terangadrive.identity_service.domain.ports.output.UserProfileOutputPort;
import com.terangadrive.identity_service.infrastructure.adapters.input.dtos.AuthResponse;
import com.terangadrive.identity_service.infrastructure.adapters.input.dtos.LoginPinRequest;
import com.terangadrive.identity_service.infrastructure.adapters.input.dtos.LoginRequest;
import com.terangadrive.identity_service.infrastructure.adapters.input.dtos.UserProfileResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService implements LoginUseCase {

    private static final int MAX_PIN_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 15;

    private final UserOutputPort userOutputPort;
    private final UserProfileOutputPort userProfileOutputPort;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserOutputPort userOutputPort,
            UserProfileOutputPort userProfileOutputPort,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userOutputPort = userOutputPort;
        this.userProfileOutputPort = userProfileOutputPort;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // Gardé pour compatibilité avec LoginUseCase - peut être supprimé si plus utilisé du tout
    @Override
    public AuthResponse execute(LoginRequest request) {
        throw new UnsupportedOperationException("Utilisez login-pin à la place");
    }

    public AuthResponse refresh(String refreshToken) {
        if (!jwtService.isTokenValid(refreshToken)) {
            throw new IllegalArgumentException("Refresh token invalide ou expiré");
        }
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("Token fourni n'est pas un refresh token");
        }
        if (jwtService.isBlacklisted(refreshToken)) {
            throw new IllegalArgumentException("Refresh token invalide ou expiré");
        }

        UUID userId = jwtService.extractUserId(refreshToken);

        User user = userOutputPort.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        var profile = userProfileOutputPort.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Profil introuvable"));

        String role = profile.getRole() != null ? profile.getRole().name() : "USER";
        String newAccessToken = jwtService.generateAccessToken(userId, user.getEmail(), role);

        UserProfileResponse profileResponse = new UserProfileResponse(
                profile.getId(),
                profile.getFirstName(),
                profile.getLastName(),
                user.getEmail(),
                profile.getPhoneNumber(),
                profile.getRole(),
                profile.getCreatedAt()
        );

        return new AuthResponse(newAccessToken, refreshToken, jwtService.getAccessTokenExpiration(), profileResponse);
    }

    public void logout(String refreshToken) {
        if (!jwtService.isTokenValid(refreshToken) || !jwtService.isRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("Refresh token invalide");
        }
        jwtService.blacklist(refreshToken);
    }

    public void setPin(UUID userId, String pin) {
        String hashedPin = passwordEncoder.encode(pin);
        userOutputPort.updatePin(userId, hashedPin);
    }

    public AuthResponse loginWithPin(LoginPinRequest request) {
        var user = userOutputPort.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Email ou PIN incorrect"));

        if (user.getPinLockedUntil() != null && LocalDateTime.now().isBefore(user.getPinLockedUntil())) {
            long minutesLeft = java.time.Duration.between(LocalDateTime.now(), user.getPinLockedUntil()).toMinutes();
            throw new IllegalArgumentException("Compte verrouillé. Réessayez dans " + (minutesLeft + 1) + " minute(s)");
        }

        if (user.getPinCode() == null) {
            throw new IllegalArgumentException("Aucun PIN configuré. Configurez votre PIN après inscription");
        }

        if (!passwordEncoder.matches(request.getPin(), user.getPinCode())) {
            userOutputPort.incrementPinAttempts(user.getId());
            int attempts = (user.getPinAttempts() == null ? 0 : user.getPinAttempts()) + 1;

            if (attempts >= MAX_PIN_ATTEMPTS) {
                LocalDateTime lockedUntil = LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES);
                userOutputPort.lockPin(user.getId(), lockedUntil);
                throw new IllegalArgumentException("Trop de tentatives. Compte verrouillé pendant " + LOCK_DURATION_MINUTES + " minutes");
            }

            int remaining = MAX_PIN_ATTEMPTS - attempts;
            throw new IllegalArgumentException("PIN incorrect. " + remaining + " tentative(s) restante(s)");
        }

        userOutputPort.resetPinAttempts(user.getId());
        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        var profile = userProfileOutputPort.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Profil introuvable"));

        String role = profile.getRole() != null ? profile.getRole().name() : "USER";

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), role);
        String refreshToken = jwtService.generateRefreshToken(user.getId());

        UserProfileResponse profileResponse = new UserProfileResponse(
                profile.getId(),
                profile.getFirstName(),
                profile.getLastName(),
                user.getEmail(),
                profile.getPhoneNumber(),
                profile.getRole(),
                profile.getCreatedAt()
        );

        return new AuthResponse(accessToken, refreshToken, jwtService.getAccessTokenExpiration(), profileResponse);
    }
}