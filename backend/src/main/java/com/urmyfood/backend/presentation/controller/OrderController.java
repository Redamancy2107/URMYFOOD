package com.urmyfood.backend.presentation.controller;

import com.urmyfood.backend.application.dto.ApiResponse;
import com.urmyfood.backend.application.dto.CancelOrderRequest;
import com.urmyfood.backend.application.dto.CheckoutRequest;
import com.urmyfood.backend.application.dto.CreateOrderReviewRequest;
import com.urmyfood.backend.application.dto.DirectCheckoutRequest;
import com.urmyfood.backend.application.dto.OrderReviewResponse;
import com.urmyfood.backend.application.dto.OrderResponse;
import com.urmyfood.backend.application.dto.ReorderResponse;
import com.urmyfood.backend.application.dto.UpdateOrderStatusRequest;
import com.urmyfood.backend.application.service.OrderService;
import com.urmyfood.backend.infrastructure.security.CustomAccountDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PostMapping("/direct-checkout")
    public ResponseEntity<ApiResponse<OrderResponse>> directCheckout(
            Authentication authentication,
            @Valid @RequestBody DirectCheckoutRequest request
    ) {
        Long accountId = getAccountId(authentication);
        return ResponseEntity.ok(ApiResponse.success(
                "Dat hang thanh cong",
                orderService.directCheckout(accountId, request)
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

    @GetMapping("/{orderId}/review")
    public ResponseEntity<ApiResponse<OrderReviewResponse>> getReview(
            Authentication authentication,
            @PathVariable UUID orderId
    ) {
        Long accountId = getAccountId(authentication);
        return ResponseEntity.ok(ApiResponse.success(
                "Review loaded",
                orderService.getReview(accountId, orderId)
        ));
    }

    @PostMapping("/{orderId}/review")
    public ResponseEntity<ApiResponse<OrderReviewResponse>> createReview(
            Authentication authentication,
            @PathVariable UUID orderId,
            @Valid @RequestBody CreateOrderReviewRequest request
    ) {
        Long accountId = getAccountId(authentication);
        return ResponseEntity.ok(ApiResponse.success(
                "Review created",
                orderService.createReview(accountId, orderId, request)
        ));
    }

    @PostMapping("/{orderId}/reorder")
    public ResponseEntity<ApiResponse<ReorderResponse>> reorder(
            Authentication authentication,
            @PathVariable UUID orderId
    ) {
        Long accountId = getAccountId(authentication);
        return ResponseEntity.ok(ApiResponse.success(
                "Items added to cart",
                orderService.reorder(accountId, orderId)
        ));
    }

    @PreAuthorize("hasRole('SHOP')")
    @GetMapping("/shop/me")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getShopOrders(Authentication authentication) {
        Long accountId = getAccountId(authentication);
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách đơn hàng của quán thành công",
                orderService.getShopOrders(accountId)
        ));
    }

    @PreAuthorize("hasRole('SHOP')")
    @GetMapping("/shop/me/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getShopOrderDetail(
            Authentication authentication,
            @PathVariable UUID orderId
    ) {
        Long accountId = getAccountId(authentication);
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy chi tiết đơn hàng thành công",
                orderService.getShopOrderDetail(accountId, orderId)
        ));
    }

    @PreAuthorize("hasRole('SHOP')")
    @PutMapping("/shop/me/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            Authentication authentication,
            @PathVariable UUID orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        Long accountId = getAccountId(authentication);
        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật trạng thái đơn hàng thành công",
                orderService.updateOrderStatus(accountId, orderId, request)
        ));
    }

    private Long getAccountId(Authentication authentication) {
        CustomAccountDetails details = (CustomAccountDetails) authentication.getPrincipal();
        return details.getAccount().getId();
    }
}
