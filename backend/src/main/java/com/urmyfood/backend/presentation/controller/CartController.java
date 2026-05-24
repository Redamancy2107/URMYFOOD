package com.urmyfood.backend.presentation.controller;

import com.urmyfood.backend.application.dto.AddCartItemRequest;
import com.urmyfood.backend.application.dto.ApiResponse;
import com.urmyfood.backend.application.dto.CartResponse;
import com.urmyfood.backend.application.dto.UpdateCartItemRequest;
import com.urmyfood.backend.application.service.CartService;
import com.urmyfood.backend.infrastructure.security.CustomAccountDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(Authentication authentication) {
        Long accountId = getAccountId(authentication);
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy giỏ hàng thành công",
                cartService.getCart(accountId)
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            Authentication authentication,
            @Valid @RequestBody AddCartItemRequest request
    ) {
        Long accountId = getAccountId(authentication);
        return ResponseEntity.ok(ApiResponse.success(
                "Thêm món vào giỏ hàng thành công",
                cartService.addItem(accountId, request)
        ));
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateItem(
            Authentication authentication,
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        Long accountId = getAccountId(authentication);
        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật giỏ hàng thành công",
                cartService.updateItem(accountId, itemId, request)
        ));
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<ApiResponse<Void>> deleteItem(
            Authentication authentication,
            @PathVariable UUID itemId
    ) {
        Long accountId = getAccountId(authentication);
        cartService.deleteItem(accountId, itemId);
        return ResponseEntity.ok(ApiResponse.success("Xóa món khỏi giỏ hàng thành công", null));
    }

    private Long getAccountId(Authentication authentication) {
        CustomAccountDetails details = (CustomAccountDetails) authentication.getPrincipal();
        return details.getAccount().getId();
    }
}
