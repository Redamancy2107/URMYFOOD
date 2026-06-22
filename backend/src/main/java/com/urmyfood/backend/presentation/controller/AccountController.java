package com.urmyfood.backend.presentation.controller;

import com.urmyfood.backend.application.dto.AccountProfileDto;
import com.urmyfood.backend.application.dto.ApiResponse;
import com.urmyfood.backend.application.service.AccountService;
import com.urmyfood.backend.domain.model.Account;
import com.urmyfood.backend.infrastructure.security.CustomAccountDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<ApiResponse<Account>> createAccount(@RequestBody Account account) {
        return ResponseEntity.ok(ApiResponse.success("Tạo tài khoản thành công", accountService.createAccount(account)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Account>> getAccount(@PathVariable Long id) {
        return accountService.getAccountById(id)
                .map(account -> ResponseEntity.ok(ApiResponse.success("Lấy thông tin tài khoản thành công", account)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AccountProfileDto>> getMyProfile(Authentication authentication) {
        CustomAccountDetails details = (CustomAccountDetails) authentication.getPrincipal();
        Account account = details.getAccount();
        AccountProfileDto dto = AccountProfileDto.builder()
                .id(account.getId())
                .fullName(account.getFullName())
                .email(account.getEmail())
                .phone(account.getPhone())
                .role(account.getRole())
                .avatarUrl(account.getAvatarUrl())
                .build();
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin tài khoản thành công", dto));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<AccountProfileDto>> updateMyProfile(
            Authentication authentication,
            @RequestBody com.urmyfood.backend.application.dto.UpdateProfileRequest request
    ) {
        CustomAccountDetails details = (CustomAccountDetails) authentication.getPrincipal();
        Account updatedAccount = accountService.updateProfile(details.getAccount().getId(), request);
        AccountProfileDto dto = AccountProfileDto.builder()
                .id(updatedAccount.getId())
                .fullName(updatedAccount.getFullName())
                .email(updatedAccount.getEmail())
                .phone(updatedAccount.getPhone())
                .role(updatedAccount.getRole())
                .avatarUrl(updatedAccount.getAvatarUrl())
                .build();
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin tài khoản thành công", dto));
    }

    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AccountProfileDto>> uploadAvatar(
            Authentication authentication,
            @RequestParam("file") MultipartFile file
    ) {
        CustomAccountDetails details = (CustomAccountDetails) authentication.getPrincipal();
        AccountProfileDto dto = accountService.updateUserAvatar(details.getAccount().getId(), file);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật avatar thành công", dto));
    }

    @PutMapping("/me/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            Authentication authentication,
            @RequestBody com.urmyfood.backend.application.dto.ChangePasswordRequest request
    ) {
        CustomAccountDetails details = (CustomAccountDetails) authentication.getPrincipal();
        accountService.changePassword(details.getAccount().getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Đổi mật khẩu thành công", null));
    }
}
