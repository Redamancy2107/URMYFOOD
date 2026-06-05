package com.urmyfood.backend.presentation.controller;

import com.urmyfood.backend.application.dto.ApiResponse;
import com.urmyfood.backend.application.dto.ShopVerificationRequest;
import com.urmyfood.backend.application.dto.ShopVerificationResponse;
import com.urmyfood.backend.application.service.ShopVerificationService;
import com.urmyfood.backend.infrastructure.security.CustomAccountDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/shops")
@RequiredArgsConstructor
public class ShopController {

    private final ShopVerificationService shopVerificationService;

    @PostMapping("/me/verification")
    public ResponseEntity<ApiResponse<ShopVerificationResponse>> submitMyVerification(
            Authentication authentication,
            @Valid @RequestBody ShopVerificationRequest request
    ) {
        ShopVerificationResponse response = shopVerificationService.submit(getAccountId(authentication), request);
        return ResponseEntity.ok(ApiResponse.success("Hồ sơ quán đã được gửi để chờ xác minh", response));
    }

    @GetMapping("/me/verification")
    public ResponseEntity<ApiResponse<ShopVerificationResponse>> getMyVerification(Authentication authentication) {
        ShopVerificationResponse response = shopVerificationService.getMyVerification(getAccountId(authentication));
        return ResponseEntity.ok(ApiResponse.success("Lấy hồ sơ xác minh quán thành công", response));
    }

    private Long getAccountId(Authentication authentication) {
        CustomAccountDetails details = (CustomAccountDetails) authentication.getPrincipal();
        return details.getAccount().getId();
    }
}
