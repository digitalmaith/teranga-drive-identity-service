package com.terangadrive.identity_service.infrastructure.adapters.input.controllers;

import com.terangadrive.identity_service.domain.models.User;
import com.terangadrive.identity_service.domain.models.UserProfile;
import com.terangadrive.identity_service.domain.ports.input.CreateUserProfileUseCase;
import com.terangadrive.identity_service.domain.ports.output.UserOutputPort;
import com.terangadrive.identity_service.domain.ports.output.UserProfileOutputPort;
import com.terangadrive.identity_service.domain.services.JwtService;
import com.terangadrive.identity_service.domain.services.OtpService;
import com.terangadrive.identity_service.domain.services.UserProfileService;
import com.terangadrive.identity_service.infrastructure.adapters.input.dtos.AuthResponse;
import com.terangadrive.identity_service.infrastructure.adapters.input.dtos.CreateUserRequest;
import com.terangadrive.identity_service.infrastructure.adapters.input.dtos.UserProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User Management", description = "Endpoints pour la gestion des inscriptions et des profils Teranga Drive")
public class UserProfileController {

    private final CreateUserProfileUseCase createUserProfileUseCase;
    private final OtpService otpService;
    private final UserProfileService userProfileService;
    private final JwtService jwtService;
    private final UserProfileOutputPort userProfileOutputPort;
    private final UserOutputPort userOutputPort;

    public UserProfileController(
            CreateUserProfileUseCase createUserProfileUseCase,
            OtpService otpService,
            UserProfileService userProfileService,
            JwtService jwtService,
            UserProfileOutputPort userProfileOutputPort,
            UserOutputPort userOutputPort
    ) {
        this.createUserProfileUseCase = createUserProfileUseCase;
        this.otpService = otpService;
        this.userProfileService = userProfileService;
        this.jwtService = jwtService;
        this.userProfileOutputPort = userProfileOutputPort;
        this.userOutputPort = userOutputPort;
    }

    @PostMapping
    @Operation(summary = "Initier l'inscription - envoie un OTP de vérification")
    public ResponseEntity<Map<String, String>> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserProfile userToCreate = new UserProfile();
        userToCreate.setFirstName(request.getFirstName());
        userToCreate.setLastName(request.getLastName());
        userToCreate.setPhoneNumber(request.getPhoneNumber());
        userToCreate.setRole(request.getRole());
        userToCreate.setEmail(request.getEmail());

        createUserProfileUseCase.execute(userToCreate);

        return ResponseEntity.ok(Map.of(
                "message", "Un code de vérification a été envoyé à " + request.getEmail()
        ));
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Vérifier l'OTP et finaliser l'inscription - retourne les tokens (auto-login)")
    public ResponseEntity<?> verifyEmail(
            @RequestParam String email,
            @RequestParam String otp) {
        try {
            AuthResponse response = userProfileService.confirmRegistration(email, otp);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/me")
    @Operation(summary = "Récupérer le profil du user connecté")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserProfileResponse> getMyProfile(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String token = authHeader.substring(7);
        UUID userId = jwtService.extractUserId(token);

        UserProfile profile = userProfileOutputPort.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Profil introuvable"));

        User user = userOutputPort.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        UserProfileResponse response = new UserProfileResponse(
                profile.getId(),
                profile.getFirstName(),
                profile.getLastName(),
                user.getEmail(),
                profile.getPhoneNumber(),
                profile.getRole(),
                profile.getCreatedAt()
        );

        return ResponseEntity.ok(response);
    }
}