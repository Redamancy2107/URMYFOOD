package com.urmyfood.backend.presentation.controller;

import com.urmyfood.backend.application.dto.AddressDto;
import com.urmyfood.backend.application.dto.ApiResponse;
import com.urmyfood.backend.application.dto.CreateAddressRequest;
import com.urmyfood.backend.application.service.AddressService;
import com.urmyfood.backend.infrastructure.security.CustomAccountDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AddressDto>>> getMyAddresses(Authentication authentication) {
        Long accountId = getAccountId(authentication);
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách địa chỉ thành công",
                addressService.getAddressesByAccountId(accountId)
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AddressDto>> createAddress(
            Authentication authentication,
            @RequestBody CreateAddressRequest request
    ) {
        Long accountId = getAccountId(authentication);
        return ResponseEntity.ok(ApiResponse.success(
                "Thêm địa chỉ thành công",
                addressService.createAddress(accountId, request)
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AddressDto>> updateAddress(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody CreateAddressRequest request
    ) {
        Long accountId = getAccountId(authentication);
        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật địa chỉ thành công",
                addressService.updateAddress(accountId, id, request)
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            Authentication authentication,
            @PathVariable Long id
    ) {
        Long accountId = getAccountId(authentication);
        addressService.deleteAddress(accountId, id);
        return ResponseEntity.ok(ApiResponse.success("Xóa địa chỉ thành công", null));
    }

    @PutMapping("/{id}/default")
    public ResponseEntity<ApiResponse<AddressDto>> setDefault(
            Authentication authentication,
            @PathVariable Long id
    ) {
        Long accountId = getAccountId(authentication);
        return ResponseEntity.ok(ApiResponse.success(
                "Đặt địa chỉ mặc định thành công",
                addressService.setDefault(accountId, id)
        ));
    }

    private Long getAccountId(Authentication authentication) {
        CustomAccountDetails details = (CustomAccountDetails) authentication.getPrincipal();
        return details.getAccount().getId();
    }
}
