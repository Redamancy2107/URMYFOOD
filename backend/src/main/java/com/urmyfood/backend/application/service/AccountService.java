package com.urmyfood.backend.application.service;

import com.urmyfood.backend.domain.model.Account;
import com.urmyfood.backend.domain.repository.AccountRepository;
import com.urmyfood.backend.application.dto.ChangePasswordRequest;
import com.urmyfood.backend.application.dto.UpdateProfileRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public void changePassword(Long id, ChangePasswordRequest request) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), account.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không chính xác");
        }

        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
        accountRepository.save(account);
    }

    public Account createAccount(Account account) {
        return accountRepository.save(account);
    }

    public Optional<Account> getAccountById(Long id) {
        return accountRepository.findById(id);
    }

    public Account updateProfile(Long id, UpdateProfileRequest request) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));
        
        if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
            account.setFullName(request.getFullName().trim());
        }
        
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            String newPhone = request.getPhone().trim();
            if (!newPhone.equals(account.getPhone())) {
                accountRepository.findByPhone(newPhone).ifPresent(other -> {
                    throw new IllegalArgumentException("Số điện thoại này đã được sử dụng");
                });
                account.setPhone(newPhone);
            }
        }

        if (request.getAvatarUrl() != null) {
            account.setAvatarUrl(request.getAvatarUrl().trim());
        }
        
        return accountRepository.save(account);
    }
}
