package com.urmyfood.backend.presentation.controller;

import com.urmyfood.backend.application.dto.ApiResponse;
import com.urmyfood.backend.application.dto.CancelOrderRequest;
import com.urmyfood.backend.application.dto.CheckoutRequest;
import com.urmyfood.backend.application.dto.OrderResponse;
import com.urmyfood.backend.application.service.OrderService;
import com.urmyfood.backend.infrastructure.security.CustomAccountDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<OrderResponse>> checkout(
            Authentication authentication,
            @Valid @RequestBody CheckoutRequest request
    ) {
        Long accountId = getAccountId(authentication);
        return ResponseEntity.ok(ApiResponse.success(
                "Đặt hàng thành công",
                orderService.checkout(accountId, request)
        ));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders(Authentication authentication) {
        Long accountId = getAccountId(authentication);
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách đơn hàng thành công",
                orderService.getMyOrders(accountId)
        ));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderDetail(
            Authentication authentication,
            @PathVariable UUID orderId
    ) {
        Long accountId = getAccountId(authentication);
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy chi tiết đơn hàng thành công",
                orderService.getOrderDetail(accountId, orderId)
        ));
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            Authentication authentication,
            @PathVariable UUID orderId,
            @Valid @RequestBody CancelOrderRequest request
    ) {
        Long accountId = getAccountId(authentication);
        return ResponseEntity.ok(ApiResponse.success(
                "Hủy đơn hàng thành công",
                orderService.cancelOrder(accountId, orderId, request)
        ));
    }

    private Long getAccountId(Authentication authentication) {
        CustomAccountDetails details = (CustomAccountDetails) authentication.getPrincipal();
        return details.getAccount().getId();
    }
}
