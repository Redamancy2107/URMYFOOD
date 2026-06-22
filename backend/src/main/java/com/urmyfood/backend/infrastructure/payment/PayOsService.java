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

    public String cancelPaymentLink(Long orderCode, String reason) {
        ensureConfigured();
        try {
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            String url = "https://api-merchant.payos.vn/v2/payment-requests/" + orderCode + "/cancel";

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            headers.set("x-client-id", clientId);
            headers.set("x-api-key", apiKey);

            String body = "{\"cancellationReason\": \"" + reason + "\"}";
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(body, headers);

            org.springframework.http.ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            log.info("Đã hủy mã thanh toán PayOS (orderCode = {}), response: {}", orderCode, response.getBody());
            return response.getBody();
        } catch (Exception e) {
            log.error("Lỗi hủy mã thanh toán PayOS (orderCode = {}): {}", orderCode, e.getMessage());
            return null;
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
