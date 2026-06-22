package com.urmyfood.backend.presentation.controller;

import com.urmyfood.backend.application.dto.ApiResponse;
import com.urmyfood.backend.application.dto.OrderResponse;
import com.urmyfood.backend.application.service.OrderService;
import com.urmyfood.backend.domain.model.Order;
import com.urmyfood.backend.domain.model.OrderStatus;
import com.urmyfood.backend.domain.model.PaymentMethod;
import com.urmyfood.backend.domain.model.PaymentStatus;
import com.urmyfood.backend.infrastructure.payment.PayOsService;
import com.urmyfood.backend.infrastructure.security.CustomAccountDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.webhooks.Webhook;
import vn.payos.model.webhooks.WebhookData;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

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

    @org.springframework.beans.factory.annotation.Value("${app.order.payment-timeout-minutes:15}")
    private int paymentExpireMinutes;

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/payos/create/{orderId}")
    public ResponseEntity<ApiResponse<CreatePaymentLinkResponse>> createPayOsPayment(
            @PathVariable UUID orderId,
            Authentication authentication) {
        
        Long customerId = getAccountId(authentication);
        Order order = orderService.findOrderByIdAndCustomer(orderId, customerId);

        if (order.getPaymentMethod() != PaymentMethod.VIETQR) {
            throw new IllegalArgumentException("Chỉ đơn VietQR mới có thể tạo mã thanh toán.");
        }
        if (order.getOrderStatus() == OrderStatus.PENDING) {
            throw new IllegalArgumentException("Vui lòng chờ quán xác nhận đơn hàng trước khi thanh toán.");
        }
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new IllegalArgumentException("Đơn hàng này đã được thanh toán.");
        }
        if (isTerminalStatus(order.getOrderStatus())) {
            throw new IllegalArgumentException("Không thể tạo mã thanh toán cho đơn hàng đã kết thúc.");
        }

        int amount = order.getFinalAmount().intValue();
        String description = "Don hang " + orderId.toString().substring(0, 8);
        if (hasCachedPayOsPayload(order)) {
            CreatePaymentLinkResponse cachedData = CreatePaymentLinkResponse.builder()
                    .bin("")
                    .accountNumber("")
                    .accountName("")
                    .amount(Long.valueOf(amount))
                    .description(description)
                    .orderCode(order.getPayosOrderCode())
                    .currency("VND")
                    .paymentLinkId("")
                    .status(vn.payos.model.v2.paymentRequests.PaymentLinkStatus.PENDING)
                    .checkoutUrl(order.getPayosCheckoutUrl())
                    .qrCode(order.getPayosQrCode())
                    .build();
            return ResponseEntity.ok(ApiResponse.success("Tạo mã thanh toán thành công", cachedData));
        }

        long orderCode = generateUniqueOrderCode();
        String returnUrl = "https://urmyfood.com/payment-result";
        String cancelUrl = "https://urmyfood.com/payment-result";

        PaymentLinkItem item = PaymentLinkItem.builder()
                .name("Order " + orderId.toString().substring(0, 8))
                .price(Long.valueOf(amount))
                .quantity(1)
                .build();

        CreatePaymentLinkRequest paymentData = CreatePaymentLinkRequest.builder()
                .orderCode(orderCode)
                .amount(Long.valueOf(amount))
                .description(description)
                .returnUrl(returnUrl)
                .cancelUrl(cancelUrl)
                .items(java.util.List.of(item))
                .build();

        CreatePaymentLinkResponse data = payOsService.createPaymentLink(paymentData);
        Long responseOrderCode = data.getOrderCode() == null ? orderCode : data.getOrderCode();
        orderService.savePayosPaymentData(orderId, responseOrderCode, data.getCheckoutUrl(), data.getQrCode());
        
        return ResponseEntity.ok(ApiResponse.success("Tạo mã thanh toán thành công", data));
    }

    private boolean hasCachedPayOsPayload(Order order) {
        return order.getPayosOrderCode() != null
                && !isBlank(order.getPayosCheckoutUrl())
                && !isBlank(order.getPayosQrCode());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private long generateUniqueOrderCode() {
        for (int attempt = 0; attempt < 5; attempt++) {
            long orderCode = ThreadLocalRandom.current().nextLong(100_000_000L, 9_000_000_000L);
            if (!orderService.existsPayosOrderCode(orderCode)) {
                return orderCode;
            }
        }
        throw new IllegalStateException("Không thể tạo mã thanh toán duy nhất");
    }

    private boolean isTerminalStatus(OrderStatus status) {
        return status == OrderStatus.COMPLETED
                || status == OrderStatus.CANCELLED
                || status == OrderStatus.REJECTED
                || status == OrderStatus.EXPIRED;
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/payos/status/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> checkPayOsStatus(
            @PathVariable UUID orderId,
            Authentication authentication) {

        Long customerId = getAccountId(authentication);
        Order order = orderService.findOrderByIdAndCustomer(orderId, customerId);

        if (order.getPaymentStatus() != PaymentStatus.PAID && order.getPayosOrderCode() != null) {
            String status = payOsService.getPaymentStatus(order.getPayosOrderCode());
            if ("PAID".equalsIgnoreCase(status)) {
                orderService.markOrderAsPaidByOrderCode(order.getPayosOrderCode());
            }
        }

        OrderResponse response = orderService.getOrderDetail(customerId, orderId);
        return ResponseEntity.ok(ApiResponse.success("Lấy trạng thái thanh toán thành công", response));
    }

    @PostMapping("/payos/webhook")
    public ResponseEntity<ApiResponse<String>> handlePayOsWebhook(@RequestBody String rawBody) {
        try {
            log.info("Nhận raw webhook từ PayOS: {}", rawBody);
            
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            Webhook webhook = mapper.readValue(rawBody, Webhook.class);
            
            WebhookData data = payOsService.verifyPaymentWebhookData(webhook);
            
            if ("00".equals(data.getCode())) {
                long orderCode = data.getOrderCode();
                if (orderCode == 123) {
                    log.info("Nhận webhook test từ PayOS (orderCode = 123). Bỏ qua xử lý đơn hàng.");
                    return ResponseEntity.ok(ApiResponse.success("Test webhook thành công", "OK"));
                }
                
                log.info("Thanh toán thành công cho orderCode: {}", orderCode);
                try {
                    orderService.markOrderAsPaidByOrderCode(orderCode);
                } catch (IllegalArgumentException e) {
                    log.warn("Webhook hợp lệ nhưng không thể cập nhật đơn hàng: {}", e.getMessage());
                    return ResponseEntity.ok(ApiResponse.success("Webhook hợp lệ nhưng không tìm thấy đơn", "OK"));
                }
            }
            return ResponseEntity.ok(ApiResponse.success("Xử lý webhook thành công", "OK"));
        } catch (Exception e) {
            log.error("Lỗi xử lý webhook PayOS: ", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Lỗi xác thực webhook"));
        }
    }
}
