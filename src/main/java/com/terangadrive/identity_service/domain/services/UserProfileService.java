package com.terangadrive.identity_service.domain.services;

import com.terangadrive.identity_service.domain.models.User;
import com.terangadrive.identity_service.domain.models.UserProfile;
import com.terangadrive.identity_service.domain.ports.input.CreateUserProfileUseCase;
import com.terangadrive.identity_service.domain.ports.output.UserOutputPort;
import com.terangadrive.identity_service.domain.ports.output.UserProfileOutputPort;
import jakarta.persistence.EntityManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserProfileService implements CreateUserProfileUseCase {

    private final UserProfileOutputPort userProfileOutputPort;
    private final UserOutputPort userOutputPort;
    private final EntityManager entityManager;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;

    public UserProfileService(
            UserProfileOutputPort userProfileOutputPort,
            UserOutputPort userOutputPort,
            EntityManager entityManager,
            PasswordEncoder passwordEncoder,
            OtpService otpService) {
        this.userProfileOutputPort = userProfileOutputPort;
        this.userOutputPort = userOutputPort;
        this.entityManager = entityManager;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
    }

    // Étape 1 : valider et envoyer OTP, ne PAS créer en DB
    @Override
    public UserProfile execute(UserProfile userProfile) {

        // Vérifier si l'email existe déjà en base
        if (userOutputPort.existsByEmail(userProfile.getEmail())) {
            throw new IllegalArgumentException("Cet email est déjà associé à un compte");
        }

        // 2. Vérifier si le numéro de téléphone existe déjà en base
        if (userProfileOutputPort.existsByPhoneNumber(userProfile.getPhoneNumber())) {
            throw new IllegalArgumentException("Ce numéro de téléphone est déjà utilisé");
        }

        // Hacher le password avant stockage temporaire
        userProfile.setPassword(passwordEncoder.encode(userProfile.getPassword()));

        // Stocker temporairement et envoyer l'OTP
        otpService.generateAndSend(userProfile.getEmail(), userProfile);

        // Retourner le profil sans ID (pas encore en DB)
        return userProfile;
    }

    // Étape 2 : vérifier OTP et créer le user en DB
    @Transactional
    public UserProfile confirmRegistration(String email, String otp) {
        UserProfile pendingUser = otpService.verifyAndGetPending(email, otp);

        if (pendingUser == null) {
            throw new IllegalArgumentException("OTP invalide ou expiré");
        }

        UUID userId = UUID.randomUUID();

        // Créer le user en DB
        User user = new User(userId, pendingUser.getEmail(), pendingUser.getPassword());
        userOutputPort.save(user);
        entityManager.flush();

        // Créer le profil en DB
        pendingUser.setId(userId);
        pendingUser.setCreatedAt(LocalDateTime.now());
        return userProfileOutputPort.save(pendingUser);
    }
}