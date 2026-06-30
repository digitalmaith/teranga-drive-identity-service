package com.terangadrive.identity_service.domain.services;

import com.terangadrive.identity_service.domain.models.User;
import com.terangadrive.identity_service.domain.ports.input.LoginUseCase;
import com.terangadrive.identity_service.domain.ports.output.UserOutputPort;
import com.terangadrive.identity_service.domain.ports.output.UserProfileOutputPort;
import com.terangadrive.identity_service.infrastructure.adapters.input.dtos.AuthResponse;
import com.terangadrive.identity_service.infrastructure.adapters.input.dtos.LoginRequest;
import com.terangadrive.identity_service.infrastructure.adapters.input.dtos.UserProfileResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class AuthService implements LoginUseCase {

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

    @Override
    public AuthResponse execute(LoginRequest request) {
        // 1. Vérifier que l'email existe
        var user = userOutputPort.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Email ou mot de passe incorrect"));

        // 2. Vérifier le mot de passe
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Email ou mot de passe incorrect");
        }

        // 3. Récupérer le profil
        var profile = userProfileOutputPort.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Profil introuvable"));

        String role = profile.getRole() != null ? profile.getRole().name() : "USER";

        // 4. Générer les tokens
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), role);
        String refreshToken = jwtService.generateRefreshToken(user.getId());

        // 5. Construire le profil de réponse (sans password)
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

    public AuthResponse refresh(String refreshToken) {
        // 1. Vérifier que le token est valide
        if (!jwtService.isTokenValid(refreshToken)) {
            throw new IllegalArgumentException("Refresh token invalide ou expiré");
        }

        // 2. Vérifier que c'est bien un refresh token
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("Token fourni n'est pas un refresh token");
        }

        // 3. Extraire l'userId
        UUID userId = jwtService.extractUserId(refreshToken);

        // 4. Récupérer le user
        User user = userOutputPort.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        // 5. Récupérer le profil
        var profile = userProfileOutputPort.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Profil introuvable"));

        String role = profile.getRole() != null ? profile.getRole().name() : "USER";

        // 6. Générer un nouvel access token
        String newAccessToken = jwtService.generateAccessToken(userId, user.getEmail(), role);

        // 7. Construire la réponse
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

    public void logout(String refreshToken){
        // Vérifier que c'est un refresh token valide
        if (!jwtService.isTokenValid(refreshToken) || !jwtService.isRefreshToken(refreshToken)){
            throw new IllegalArgumentException("Refresh token invalide");
        }

        // Blacklister le token
        jwtService.blacklist(refreshToken);
    }
}