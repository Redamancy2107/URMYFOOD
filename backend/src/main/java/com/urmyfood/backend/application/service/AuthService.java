package com.urmyfood.backend.application.service;

import com.urmyfood.backend.application.dto.AuthResponse;
import com.urmyfood.backend.application.dto.LoginRequest;
import com.urmyfood.backend.application.dto.RegisterRequest;
import com.urmyfood.backend.domain.model.Account;
import com.urmyfood.backend.domain.repository.AccountRepository;
import com.urmyfood.backend.infrastructure.security.CustomAccountDetails;
import com.urmyfood.backend.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import java.util.Collections;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class AuthService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final OtpService otpService;

    @Value("${google.client-id}")
    private String googleClientId;


    public AuthResponse register(RegisterRequest request) {
        if (accountRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        if (accountRepository.findByPhone(request.getPhone()).isPresent()) {
            throw new RuntimeException("Phone number already exists");
        }

        if (!otpService.verifyOtp(request.getEmail(), request.getOtpCode())) {
            throw new RuntimeException("Mã OTP không chính xác hoặc đã hết hạn");
        }

        Account account = Account.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : "CUSTOMER")
                .build();

        accountRepository.save(account);
        
        CustomAccountDetails accountDetails = new CustomAccountDetails(account);
        String token = jwtService.generateToken(accountDetails);
        
        return AuthResponse.builder()
                .token(token)
                .fullName(account.getFullName())
                .role(account.getRole())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        Account account = accountRepository.findByEmail(request.getEmailOrPhone())
                .or(() -> accountRepository.findByPhone(request.getEmailOrPhone()))
                .orElseThrow(() -> new RuntimeException("Account not found"));

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        account.getEmail(),
                        request.getPassword()
                )
        );

        return generateAuthResponse(account);
    }

    public void sendOtp(String email) {
        otpService.sendOtp(email);
    }

    public AuthResponse loginWithGoogle(String idTokenString) {
        log.info("Receiving Google ID Token");
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                log.error("Invalid Google ID Token (verifier returned null)");
                throw new RuntimeException("Invalid Google ID Token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            log.info("Google Login Successful for email: {}", email);

            Account account = accountRepository.findByEmail(email)
                    .orElseGet(() -> {
                        log.info("Creating new account for Google user: {}", email);
                        Account newAccount = Account.builder()
                                .email(email)
                                .fullName(name)
                                .role("CUSTOMER")
                                .password(passwordEncoder.encode(java.util.UUID.randomUUID().toString())) // Secure random dummy password
                                .build();
                        return accountRepository.save(newAccount);
                    });

            return generateAuthResponse(account);
        } catch (Exception e) {
            log.error("Google Authentication failed: {}", e.getMessage(), e);
            throw new RuntimeException("Google Authentication failed: " + e.getMessage());
        }
    }

    public AuthResponse loginWithOtp(String email, String code) {
        if (!otpService.verifyOtp(email, code)) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        Account account = accountRepository.findByEmail(email)
                .orElseGet(() -> {
                    Account newAccount = Account.builder()
                            .email(email)
                            .fullName("User " + email.split("@")[0])
                            .role("CUSTOMER")
                            .password(passwordEncoder.encode(java.util.UUID.randomUUID().toString()))
                            .build();
                    return accountRepository.save(newAccount);
                });

        return generateAuthResponse(account);
    }

    public void forgotPassword(String email) {
        accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Account not found with this email"));
        otpService.sendOtp(email);
    }

    public String verifyOtpForReset(String email, String code) {
        if (!otpService.verifyOtp(email, code)) {
            throw new RuntimeException("Invalid or expired OTP");
        }
        return jwtService.generateResetToken(email);
    }

    public void resetPassword(String resetToken, String newPassword) {
        if (!jwtService.isTokenValid(resetToken)) {
            throw new RuntimeException("Invalid or expired reset token");
        }
        String email = jwtService.extractUsername(resetToken);
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
    }

    private AuthResponse generateAuthResponse(Account account) {
        CustomAccountDetails accountDetails = new CustomAccountDetails(account);
        String token = jwtService.generateToken(accountDetails);

        return AuthResponse.builder()
                .token(token)
                .fullName(account.getFullName())
                .role(account.getRole())
                .build();
    }
}
