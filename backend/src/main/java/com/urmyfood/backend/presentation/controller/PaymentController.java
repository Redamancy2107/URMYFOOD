package com.urmyfood.backend.presentation.controller;

import com.urmyfood.backend.application.dto.ApiResponse;
import com.urmyfood.backend.application.service.OrderService;
import com.urmyfood.backend.domain.model.Order;
import com.urmyfood.backend.domain.model.PaymentStatus;
import com.urmyfood.backend.infrastructure.payment.PayOsService;
import com.urmyfood.backend.infrastructure.security.CustomAccountDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.ItemData;
import vn.payos.type.PaymentData;
import vn.payos.type.Webhook;
import vn.payos.type.WebhookData;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PayOsService payOsService;
    private final OrderService orderService;

    private Long getAccountId(Authentication authentication) {
        CustomAccountDetails details = (CustomAccountDetails) authentication.getPrincipal();
        return details.getAccount().getId();
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/payos/create/{orderId}")
    public ResponseEntity<ApiResponse<CheckoutResponseData>> createPayOsPayment(
            @PathVariable UUID orderId,
            Authentication authentication) {
        
        Long customerId = getAccountId(authentication);
        Order order = orderService.findOrderByIdAndCustomer(orderId, customerId);

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new IllegalArgumentException("Đơn hàng này đã được thanh toán.");
        }

        // Tạo order code duy nhất cho PayOS (phải là số nguyên > 0 và <= 9007199254740991)
        // Dùng timestamp kết hợp hash để tránh trùng lặp và giữ số nhỏ gọn
        long orderCode = System.currentTimeMillis() % 1_000_000_000L;
        int amount = order.getFinalAmount().intValue();
        String description = "Don hang " + orderId.toString().substring(0, 8);
        String returnUrl = "urmyfood://payment-result?orderId=" + orderId.toString();
        String cancelUrl = "urmyfood://payment-result?orderId=" + orderId.toString();

        ItemData item = ItemData.builder()
                .name("Đơn hàng từ " + order.getShop().getFullName())
                .price(amount)
                .quantity(1)
                .build();

        PaymentData paymentData = PaymentData.builder()
                .orderCode(orderCode)
                .amount(amount)
                .description(description)
                .returnUrl(returnUrl)
                .cancelUrl(cancelUrl)
                .item(item)
                .build();

        CheckoutResponseData data = payOsService.createPaymentLink(paymentData);
        orderService.savePayosOrderCode(orderId, orderCode);
        
        return ResponseEntity.ok(ApiResponse.success("Tạo mã thanh toán thành công", data));
    }

    @PostMapping("/payos/webhook")
    public ResponseEntity<ApiResponse<String>> handlePayOsWebhook(@RequestBody Webhook webhookBody) {
        try {
            log.info("Nhận webhook từ PayOS: {}", webhookBody);
            WebhookData data = payOsService.verifyPaymentWebhookData(webhookBody);
            
            if ("00".equals(data.getCode())) {
                long orderCode = data.getOrderCode();
                log.info("Thanh toán thành công cho orderCode: {}", orderCode);
                // Cập nhật trạng thái đơn hàng dựa trên orderCode
                orderService.markOrderAsPaidByOrderCode(orderCode);
            }
            return ResponseEntity.ok(ApiResponse.success("Xử lý webhook thành công", "OK"));
        } catch (Exception e) {
            log.error("Lỗi xử lý webhook PayOS: ", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Lỗi xác thực webhook"));
        }
    }
}
