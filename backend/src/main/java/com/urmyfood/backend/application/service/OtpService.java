package com.urmyfood.backend.application.service;

import com.urmyfood.backend.domain.model.Otp;
import com.urmyfood.backend.domain.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpRepository otpRepository;
    private final JavaMailSender mailSender;

    public void sendOtp(String email) {
        String code = String.format("%06d", new java.security.SecureRandom().nextInt(1000000));
        Otp otp = Otp.builder()
                .email(email)
                .code(code)
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .build();
        
        otpRepository.save(otp);
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("URMYFOOD - Your OTP Code");
        message.setText("Your OTP code is: " + code + ". It will expire in 5 minutes.");
        mailSender.send(message);
    }

    public boolean verifyOtp(String email, String code) {
        return otpRepository.findLatestByEmail(email)
                .map(otp -> {
                    if (otp.isUsed() || otp.getExpiryTime().isBefore(LocalDateTime.now())) {
                        return false;
                    }
                    if (otp.getCode().equals(code)) {
                        otp.setUsed(true);
                        otpRepository.save(otp);
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }
}
