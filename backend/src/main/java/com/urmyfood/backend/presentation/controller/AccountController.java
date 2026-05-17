package com.urmyfood.backend.presentation.controller;

import com.urmyfood.backend.application.dto.AccountProfileDto;
import com.urmyfood.backend.application.dto.ApiResponse;
import com.urmyfood.backend.application.service.AccountService;
import com.urmyfood.backend.domain.model.Account;
import com.urmyfood.backend.infrastructure.security.CustomAccountDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
                .build();
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin tài khoản thành công", dto));
    }
}
