package com.urmyfood.backend.infrastructure.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.PaymentData;
import vn.payos.type.WebhookData;

import jakarta.annotation.PostConstruct;

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
        this.payOS = new PayOS(clientId, apiKey, checksumKey);
    }

    public CheckoutResponseData createPaymentLink(PaymentData paymentData) {
        try {
            return payOS.createPaymentLink(paymentData);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo mã thanh toán VietQR: " + e.getMessage());
        }
    }

    public WebhookData verifyPaymentWebhookData(vn.payos.type.Webhook webhookBody) {
        try {
            return payOS.verifyPaymentWebhookData(webhookBody);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi xác thực webhook PayOS: " + e.getMessage());
        }
    }
}
