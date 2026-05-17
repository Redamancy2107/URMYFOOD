package com.urmyfood.backend.application.service;

import java.time.LocalDateTime;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.urmyfood.backend.domain.model.Otp;
import com.urmyfood.backend.domain.repository.OtpRepository;

import lombok.RequiredArgsConstructor;

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
        message.setSubject("[URMYFOOD] Mã xác thực (OTP) đăng ký tài khoản");
        message.setText(
            "Chào bạn,\n\n" +
            "Bạn đang thực hiện đăng ký tài khoản tại ứng dụng URMYFOOD.\n" +
            "Mã xác thực (OTP) của bạn là: " + code + "\n\n" +
            "Mã này có hiệu lực trong vòng 5 phút. Vì lý do bảo mật, vui lòng không cung cấp mã này cho bất kỳ ai.\n\n" +
            "Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email này hoặc liên hệ với bộ phận hỗ trợ của chúng tôi.\n\n" +
            "Trân trọng,\n" +
            "Đội ngũ URMYFOOD"
        );
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
