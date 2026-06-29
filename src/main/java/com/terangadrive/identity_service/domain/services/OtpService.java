package com.terangadrive.identity_service.domain.services;

import com.terangadrive.identity_service.domain.models.UserProfile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private final JavaMailSender mailSender;

    // Stockage OTP : email → OtpEntry
    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();

    // Stockage temporaire des données d'inscription en attente de vérification
    private final Map<String, UserProfile> pendingRegistrations = new ConcurrentHashMap<>();

    public OtpService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void generateAndSend(String email, UserProfile pendingUser) {
        String otp = String.format("%06d", new SecureRandom().nextInt(999999));
        otpStore.put(email, new OtpEntry(otp, LocalDateTime.now().plusMinutes(10)));
        pendingRegistrations.put(email, pendingUser);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Teranga Drive - Code de vérification");
        message.setText(
                "Bonjour " + pendingUser.getFirstName() + ",\n\n" +
                        "Votre code de vérification est : " + otp + "\n\n" +
                        "Ce code expire dans 10 minutes.\n\n" +
                        "L'équipe Teranga Drive"
        );
        mailSender.send(message);
    }

    public UserProfile verifyAndGetPending(String email, String otp) {
        OtpEntry entry = otpStore.get(email);
        if (entry == null) return null;
        if (LocalDateTime.now().isAfter(entry.expiration())) {
            otpStore.remove(email);
            pendingRegistrations.remove(email);
            return null;
        }
        if (!entry.otp().equals(otp)) return null;

        // OTP valide → on récupère les données et on nettoie
        otpStore.remove(email);
        return pendingRegistrations.remove(email);
    }

    private record OtpEntry(String otp, LocalDateTime expiration) {}
}