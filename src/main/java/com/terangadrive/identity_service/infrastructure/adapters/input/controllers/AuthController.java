package com.terangadrive.identity_service.infrastructure.adapters.input.controllers;

import com.terangadrive.identity_service.domain.services.AuthService;
import com.terangadrive.identity_service.domain.services.JwtService;
import com.terangadrive.identity_service.infrastructure.adapters.input.dtos.*;
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
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Endpoints de connexion et gestion des tokens")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renouveler l'access token via le refresh token")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    @Operation(summary = "Déconnexion - invalide le refresh token")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, String>> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(Map.of("message", "Déconnexion réussie"));
    }

    @PostMapping("/set-pin")
    @Operation(summary = "Définir ou changer son code PIN")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, String>> setPin(
            @Valid @RequestBody SetPinRequest request,
            HttpServletRequest httpRequest) {

        String authHeader = httpRequest.getHeader("Authorization");
        String token = authHeader.substring(7);
        UUID userId = jwtService.extractUserId(token);

        authService.setPin(userId, request.getPin());

        return ResponseEntity.ok(Map.of("message", "PIN configuré avec succès"));
    }

    @PostMapping("/login-pin")
    @Operation(summary = "Connexion via code PIN")
    public ResponseEntity<AuthResponse> loginWithPin(@Valid @RequestBody LoginPinRequest request) {
        return ResponseEntity.ok(authService.loginWithPin(request));
    }
}