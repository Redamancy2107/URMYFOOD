package com.urmyfood.backend.presentation.controller;

import com.urmyfood.backend.application.dto.ApiResponse;
import com.urmyfood.backend.application.dto.SavedVoucherResponse;
import com.urmyfood.backend.application.dto.VoucherDto;
import com.urmyfood.backend.application.service.VoucherService;
import com.urmyfood.backend.infrastructure.security.CustomAccountDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<VoucherDto>>> getActiveVouchers(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(
                "Vouchers loaded",
                voucherService.getActiveVouchers(resolveAccountId(authentication))
        ));
    }

    @GetMapping("/saved")
    public ResponseEntity<ApiResponse<List<VoucherDto>>> getSavedVouchers(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(
                "Saved vouchers loaded",
                voucherService.getSavedVouchers(getAccountId(authentication))
        ));
    }

    @PostMapping("/{voucherId}/saved")
    public ResponseEntity<ApiResponse<SavedVoucherResponse>> saveVoucher(
            Authentication authentication,
            @PathVariable Long voucherId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Voucher saved",
                voucherService.saveVoucher(getAccountId(authentication), voucherId)
        ));
    }

    @DeleteMapping("/{voucherId}/saved")
    public ResponseEntity<ApiResponse<SavedVoucherResponse>> unsaveVoucher(
            Authentication authentication,
            @PathVariable Long voucherId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Voucher unsaved",
                voucherService.unsaveVoucher(getAccountId(authentication), voucherId)
        ));
    }

    private Long resolveAccountId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomAccountDetails details)) {
            return null;
        }
        return details.getAccount().getId();
    }

    private Long getAccountId(Authentication authentication) {
        CustomAccountDetails details = (CustomAccountDetails) authentication.getPrincipal();
        return details.getAccount().getId();
    }
}
