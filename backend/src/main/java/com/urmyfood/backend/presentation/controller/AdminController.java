package com.urmyfood.backend.presentation.controller;

import com.urmyfood.backend.application.dto.AdminProfileDto;
import com.urmyfood.backend.application.dto.AdminProfileUpdateDto;
import com.urmyfood.backend.application.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping
    public ResponseEntity<AdminProfileDto> getAdminProfile(@RequestParam("account_id") Long accountId) {
        AdminProfileDto profile = adminService.getAdminProfile(accountId);
        return ResponseEntity.ok(profile);
    }

    @PatchMapping
    public ResponseEntity<AdminProfileDto> updateAdminProfile(
            @RequestParam("account_id") Long accountId,
            @RequestBody AdminProfileUpdateDto updates) {
        AdminProfileDto updatedProfile = adminService.updateAdminProfile(accountId, updates);
        return ResponseEntity.ok(updatedProfile);
    }
}
