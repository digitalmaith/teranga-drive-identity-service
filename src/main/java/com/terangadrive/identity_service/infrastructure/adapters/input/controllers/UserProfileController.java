package com.terangadrive.identity_service.infrastructure.adapters.input.controllers;

import com.terangadrive.identity_service.domain.models.UserProfile;
import com.terangadrive.identity_service.domain.ports.input.CreateUserProfileUseCase;
import com.terangadrive.identity_service.domain.services.OtpService;
import com.terangadrive.identity_service.domain.services.UserProfileService;
import com.terangadrive.identity_service.infrastructure.adapters.input.dtos.CreateUserRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User Management" , description = "Endpoints pour la gestion des inscriptions et des profils Teranga Drive")
public class UserProfileController {

    private final CreateUserProfileUseCase createUserProfileUseCase;
    private final OtpService otpService;
    private final UserProfileService userProfileService;

    // Injection du cas d'utilisation par constructeur
    public UserProfileController(
            CreateUserProfileUseCase createUserProfileUseCase ,
            OtpService otpService,
            UserProfileService userProfileService
    ){
        this.createUserProfileUseCase = createUserProfileUseCase;
        this.otpService = otpService;
        this.userProfileService = userProfileService;

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
        userToCreate.setPassword(request.getPassword());

        createUserProfileUseCase.execute(userToCreate);

        return ResponseEntity.ok(Map.of(
                "message", "Un code de vérification a été envoyé à " + request.getEmail()
        ));
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Vérifier l'OTP et finaliser l'inscription")
    public ResponseEntity<?> verifyEmail(
            @RequestParam String email,
            @RequestParam String otp) {
        try {
            UserProfile created = userProfileService.confirmRegistration(email, otp);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}