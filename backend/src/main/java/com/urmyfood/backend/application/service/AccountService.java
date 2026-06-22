package com.urmyfood.backend.application.service;

import com.urmyfood.backend.domain.model.Account;
import com.urmyfood.backend.domain.repository.AccountRepository;
import com.urmyfood.backend.application.dto.AccountProfileDto;
import com.urmyfood.backend.application.dto.ChangePasswordRequest;
import com.urmyfood.backend.application.dto.UpdateProfileRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProfileImageStorageClient profileImageStorageClient;

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

    @Transactional
    public AccountProfileDto updateUserAvatar(Long id, MultipartFile file) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));

        String oldAvatarUrl = account.getAvatarUrl();
        String newAvatarUrl = profileImageStorageClient.uploadUserAvatar(id, file);

        account.setAvatarUrl(newAvatarUrl);
        Account saved = accountRepository.save(account);

        if (oldAvatarUrl != null) {
            try {
                profileImageStorageClient.deleteUserAvatar(id, oldAvatarUrl);
            } catch (Exception e) {
                log.warn("Failed to delete old user avatar", e);
            }
        }

        return AccountProfileDto.builder()
                .id(saved.getId())
                .fullName(saved.getFullName())
                .email(saved.getEmail())
                .phone(saved.getPhone())
                .role(saved.getRole())
                .avatarUrl(saved.getAvatarUrl())
                .build();
    }
}
