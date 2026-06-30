package com.terangadrive.identity_service.infrastructure.adapters.input.controllers;

import com.terangadrive.identity_service.domain.ports.input.LoginUseCase;
import com.terangadrive.identity_service.domain.services.AuthService;
import com.terangadrive.identity_service.infrastructure.adapters.input.dtos.AuthResponse;
import com.terangadrive.identity_service.infrastructure.adapters.input.dtos.LoginRequest;
import com.terangadrive.identity_service.infrastructure.adapters.input.dtos.RefreshTokenRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Endpoints de connexion et gestion des tokens")
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final AuthService authService;

    public AuthController(LoginUseCase loginUseCase , AuthService authService) {
        this.loginUseCase = loginUseCase;
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Connexion - retourne access token et refresh token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(loginUseCase.execute(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renouveler l'access token via le refresh token")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request.getRefreshToken()));
    }
}