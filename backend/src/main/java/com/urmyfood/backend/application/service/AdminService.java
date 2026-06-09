package com.urmyfood.backend.application.service;

import com.urmyfood.backend.application.dto.AdminProfileDto;
import com.urmyfood.backend.application.dto.AdminProfileUpdateDto;
import com.urmyfood.backend.domain.model.Account;
import com.urmyfood.backend.domain.model.Admin;
import com.urmyfood.backend.domain.repository.AccountRepository;
import com.urmyfood.backend.domain.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final AccountRepository accountRepository;

    public AdminProfileDto getAdminProfile(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found for id: " + accountId));
        
        Admin admin = adminRepository.findByAccountId(accountId)
                .orElseGet(() -> {
                    Admin emptyAdmin = new Admin();
                    emptyAdmin.setAccountId(accountId);
                    return emptyAdmin;
                });
                
        return mapToDto(admin, account);
    }

    @Transactional
    public AdminProfileDto updateAdminProfile(Long accountId, AdminProfileUpdateDto updates) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found for id: " + accountId));

        Admin admin = adminRepository.findByAccountId(accountId)
                .orElseGet(() -> {
                    // Create new if not exists
                    Admin newAdmin = new Admin();
                    newAdmin.setAccountId(accountId);
                    return newAdmin;
                });

        if (updates.getFullName() != null) account.setFullName(updates.getFullName());
        if (updates.getWorkEmail() != null) account.setEmail(updates.getWorkEmail());
        if (updates.getPhoneNumber() != null) account.setPhone(updates.getPhoneNumber());

        if (updates.getPosition() != null) admin.setPosition(updates.getPosition());
        if (updates.getShortBio() != null) admin.setShortBio(updates.getShortBio());

        accountRepository.save(account);
        Admin savedAdmin = adminRepository.save(admin);
        
        return mapToDto(savedAdmin, account);
    }

    private AdminProfileDto mapToDto(Admin admin, Account account) {
        return AdminProfileDto.builder()
                .id(admin.getId())
                .accountId(account.getId())
                .fullName(account.getFullName())
                .workEmail(account.getEmail())
                .phoneNumber(account.getPhone())
                .position(admin.getPosition())
                .shortBio(admin.getShortBio())
                .is2FaEnabled(admin.is2FaEnabled())
                .build();
    }
}
