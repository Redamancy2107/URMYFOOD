package com.urmyfood.backend.infrastructure.payment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.PaymentLink;
import vn.payos.model.webhooks.WebhookData;

import jakarta.annotation.PostConstruct;

@Slf4j
@Service
public class PayOsService {

    @Value("${payos.client-id}")
    private String clientId;

    @Value("${payos.api-key}")
    private String apiKey;

    @Value("${payos.checksum-key}")
    private String checksumKey;

    private PayOS payOS;

    @PostConstruct
    public void init() {
        if (isBlank(clientId) || isBlank(apiKey) || isBlank(checksumKey)) {
            log.warn("PayOS chưa được cấu hình (thiếu PAYOS_CLIENT_ID / PAYOS_API_KEY / PAYOS_CHECKSUM_KEY). "
                    + "Ứng dụng vẫn chạy bình thường, nhưng chức năng thanh toán VietQR sẽ không khả dụng.");
            return;
        }
        this.payOS = new PayOS(clientId.trim(), apiKey.trim(), checksumKey.trim());
        log.info("PayOS đã được khởi tạo thành công.");
    }

    public CreatePaymentLinkResponse createPaymentLink(CreatePaymentLinkRequest paymentData) {
        ensureConfigured();
        try {
            return payOS.paymentRequests().create(paymentData);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo mã thanh toán VietQR: " + e.getMessage());
        }
    }

    public String getPaymentStatus(Long orderCode) {
        ensureConfigured();
        try {
            PaymentLink data = payOS.paymentRequests().get(String.valueOf(orderCode));
            return data.getStatus().name();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi kiểm tra trạng thái thanh toán PayOS: " + e.getMessage());
        }
    }

    public WebhookData verifyPaymentWebhookData(vn.payos.model.webhooks.Webhook webhookBody) {
        ensureConfigured();
        try {
            return payOS.webhooks().verify(webhookBody);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi xác thực webhook PayOS: " + e.getMessage());
        }
    }

    private void ensureConfigured() {
        if (payOS == null) {
            throw new IllegalStateException(
                    "Thanh toán VietQR chưa được cấu hình trên máy chủ. Vui lòng dùng phương thức COD hoặc liên hệ quản trị viên.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
