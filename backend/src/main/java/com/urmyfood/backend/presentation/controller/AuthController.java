package com.urmyfood.backend.presentation.controller;

import com.urmyfood.backend.application.dto.*;
import com.urmyfood.backend.application.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Đăng ký thành công", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", response));
    }

    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<Void>> sendOtp(@RequestBody OtpRequest request) {
        authService.sendOtp(request.getEmail(), request.getPhone());
        return ResponseEntity.ok(ApiResponse.success("Mã OTP đã được gửi", null));
    }

    @PostMapping("/login-otp")
    public ResponseEntity<ApiResponse<AuthResponse>> loginOtp(@RequestBody OtpLoginRequest request) {
        AuthResponse response = authService.loginWithOtp(request.getEmail(), request.getCode());
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", response));
    }

    @PostMapping("/login-google")
    public ResponseEntity<ApiResponse<AuthResponse>> loginGoogle(@RequestBody GoogleLoginRequest request) {
        AuthResponse response = authService.loginWithGoogle(request.getIdToken());
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập Google thành công", response));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("Mã OTP đã được gửi đến email của bạn", null));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<VerifyOtpResponse>> verifyOtp(@RequestBody VerifyOtpRequest request) {
        String resetToken = authService.verifyOtpForReset(request.getEmail(), request.getOtpCode());
        return ResponseEntity.ok(ApiResponse.success("Xác thực thành công", new VerifyOtpResponse(resetToken)));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getResetToken(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("Mật khẩu đã được đặt lại thành công", null));
    }
}
