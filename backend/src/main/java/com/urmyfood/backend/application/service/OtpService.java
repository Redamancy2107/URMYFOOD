package com.urmyfood.backend.application.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.UserCredentials;

import java.io.ByteArrayOutputStream;
import java.util.Properties;
import java.util.Base64;

import com.urmyfood.backend.domain.model.Otp;
import com.urmyfood.backend.domain.repository.OtpRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final OtpRepository otpRepository;

    @Value("${google.client-id}")
    private String clientId;

    @Value("${google.client-secret}")
    private String clientSecret;

    @Value("${google.refresh-token}")
    private String refreshToken;

    @Async
    public void sendOtp(String email, String purpose) {
        String code = String.format("%06d", new java.security.SecureRandom().nextInt(1000000));
        Otp otp = Otp.builder()
                .email(email)
                .code(code)
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .build();

        otpRepository.save(otp);

        try {
            Session session = Session.getDefaultInstance(new Properties(), null);
            MimeMessage mimeMessage = new MimeMessage(session);
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject(String.format("[URMYFOOD] Mã xác thực (OTP) %s", purpose.toLowerCase()));

            String htmlContent = buildHtmlTemplate(purpose, code);
            helper.setText(htmlContent, true);

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            mimeMessage.writeTo(buffer);
            byte[] rawMessageBytes = buffer.toByteArray();
            String encodedEmail = Base64.getUrlEncoder().encodeToString(rawMessageBytes);

            Message message = new Message();
            message.setRaw(encodedEmail);

            UserCredentials credentials = UserCredentials.newBuilder()
                    .setClientId(clientId)
                    .setClientSecret(clientSecret)
                    .setRefreshToken(refreshToken)
                    .build();

            Gmail service = new Gmail.Builder(GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(), new HttpCredentialsAdapter(credentials))
                    .setApplicationName("URMYFOOD")
                    .build();

            service.users().messages().send("me", message).execute();
            log.info("Successfully sent OTP email to {} via Gmail HTTP API", email);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}", email, e);
            throw new RuntimeException("Không thể gửi email OTP qua Gmail API", e);
        }
    }

    private String buildHtmlTemplate(String purpose, String code) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "<meta charset=\"UTF-8\">\n" +
                "<style>\n" +
                "  body { font-family: 'Inter', Helvetica, Arial, sans-serif; background-color: #f4f7f6; margin: 0; padding: 0; }\n"
                +
                "  .container { max-width: 600px; margin: 40px auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.05); }\n"
                +
                "  .header { background-color: #FF6B6B; padding: 30px 20px; text-align: center; color: #ffffff; }\n" +
                "  .header h1 { margin: 0; font-size: 24px; font-weight: 700; letter-spacing: 1px; }\n" +
                "  .content { padding: 40px 30px; color: #333333; line-height: 1.6; }\n" +
                "  .content p { margin: 0 0 20px 0; font-size: 16px; }\n" +
                "  .otp-box { background-color: #f8f9fa; border: 2px dashed #FF6B6B; border-radius: 8px; padding: 20px; text-align: center; margin: 30px 0; }\n"
                +
                "  .otp-code { font-size: 36px; font-weight: 700; color: #FF6B6B; letter-spacing: 6px; margin: 0; }\n" +
                "  .footer { background-color: #f8f9fa; padding: 20px; text-align: center; color: #888888; font-size: 14px; border-top: 1px solid #eeeeee; }\n"
                +
                "  .purpose { font-weight: 600; color: #FF6B6B; }\n" +
                "</style>\n" +
                "</head>\n" +
                "<body>\n" +
                "<div class=\"container\">\n" +
                "  <div class=\"header\">\n" +
                "    <h1>URMYFOOD</h1>\n" +
                "  </div>\n" +
                "  <div class=\"content\">\n" +
                "    <p>Xin chào,</p>\n" +
                "    <p>Bạn đang thực hiện yêu cầu <span class=\"purpose\">" + purpose.toLowerCase()
                + "</span> tại ứng dụng URMYFOOD. Dưới đây là mã xác thực (OTP) của bạn:</p>\n" +
                "    <div class=\"otp-box\">\n" +
                "      <p class=\"otp-code\">" + code + "</p>\n" +
                "    </div>\n" +
                "    <p>Mã này có hiệu lực trong vòng <strong>5 phút</strong>. Vì lý do bảo mật, vui lòng không chia sẻ mã này cho bất kỳ ai.</p>\n"
                +
                "    <p>Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email này hoặc liên hệ ngay với bộ phận hỗ trợ của chúng tôi để được trợ giúp.</p>\n"
                +
                "  </div>\n" +
                "  <div class=\"footer\">\n" +
                "    <p>Trân trọng,<br>Đội ngũ URMYFOOD</p>\n" +
                "  </div>\n" +
                "</div>\n" +
                "</body>\n" +
                "</html>";
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
