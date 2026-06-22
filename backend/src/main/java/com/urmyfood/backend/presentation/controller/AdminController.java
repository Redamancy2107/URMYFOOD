package com.urmyfood.backend.presentation.controller;

import com.urmyfood.backend.application.dto.*;
import com.urmyfood.backend.application.service.AdminService;
import com.urmyfood.backend.infrastructure.security.CustomAccountDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<AdminProfileDto>> getAdminProfile(Authentication authentication) {
        CustomAccountDetails details = (CustomAccountDetails) authentication.getPrincipal();
        AdminProfileDto profile = adminService.getAdminProfile(details.getAccount().getId());
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin profile admin thành công", profile));
    }

    @PatchMapping("/profile")
    public ResponseEntity<ApiResponse<AdminProfileDto>> updateAdminProfile(
            Authentication authentication,
            @RequestBody AdminProfileUpdateDto updates) {
        CustomAccountDetails details = (CustomAccountDetails) authentication.getPrincipal();
        AdminProfileDto updatedProfile = adminService.updateAdminProfile(details.getAccount().getId(), updates);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật profile admin thành công", updatedProfile));
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AdminProfileDto>> uploadAvatar(
            Authentication authentication,
            @RequestParam("file") MultipartFile file) {
        CustomAccountDetails details = (CustomAccountDetails) authentication.getPrincipal();
        AdminProfileDto updatedProfile = adminService.updateAdminAvatar(details.getAccount().getId(), file);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật avatar admin thành công", updatedProfile));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardOverviewResponse>> getDashboardOverview() {
        DashboardOverviewResponse response = adminService.getDashboardOverview();
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin tổng quan dashboard thành công", response));
    }

    @GetMapping("/verifications/pending")
    public ResponseEntity<ApiResponse<List<ShopVerificationResponse>>> getPendingShopVerifications() {
        List<ShopVerificationResponse> verifications = adminService.getPendingShopVerifications();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách yêu cầu xét duyệt cửa hàng thành công", verifications));
    }

    @PostMapping("/verifications/{id}/approve")
    public ResponseEntity<ApiResponse<Void>> approveShopVerification(@PathVariable("id") Long id) {
        adminService.approveShopVerification(id);
        return ResponseEntity.ok(ApiResponse.success("Phê duyệt yêu cầu xét duyệt cửa hàng thành công", null));
    }

    @PostMapping("/verifications/{id}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectShopVerification(
            @PathVariable("id") Long id,
            @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        adminService.rejectShopVerification(id, reason);
        return ResponseEntity.ok(ApiResponse.success("Từ chối yêu cầu xét duyệt cửa hàng thành công", null));
    }

    @GetMapping("/accounts")
    public ResponseEntity<ApiResponse<PageResponse<AccountProfileDto>>> getAllAccounts(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "role", required = false) String role) {
        PageResponse<AccountProfileDto> response = adminService.getAllAccounts(page, size, role);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách tài khoản thành công", response));
    }

    @PostMapping("/accounts/{id}/lock-unlock")
    public ResponseEntity<ApiResponse<Void>> lockUnlockAccount(
            @PathVariable("id") Long id,
            @RequestParam("active") boolean active) {
        adminService.lockUnlockAccount(id, active);
        String action = active ? "Kích hoạt" : "Khóa";
        return ResponseEntity.ok(ApiResponse.success(action + " tài khoản thành công", null));
    }

    @GetMapping("/vouchers")
    public ResponseEntity<ApiResponse<List<VoucherDto>>> getAllVouchers() {
        List<VoucherDto> vouchers = adminService.getAllVouchers();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách mã giảm giá thành công", vouchers));
    }

    @PostMapping("/vouchers")
    public ResponseEntity<ApiResponse<VoucherDto>> createVoucher(@RequestBody VoucherDto dto) {
        VoucherDto response = adminService.createVoucher(dto);
        return ResponseEntity.ok(ApiResponse.success("Tạo mã giảm giá thành công", response));
    }

    @DeleteMapping("/vouchers/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVoucher(@PathVariable("id") Long id) {
        adminService.deleteVoucher(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa mã giảm giá thành công", null));
    }

    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<PageResponse<PostResponse>>> getAllPosts(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        PageResponse<PostResponse> response = adminService.getAllPosts(page, size);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách bài viết thành công", response));
    }

    @PostMapping("/posts/{postId}/status")
    public ResponseEntity<ApiResponse<Void>> moderatePostStatus(
            @PathVariable("postId") UUID postId,
            @RequestParam("status") String status) {
        adminService.moderatePostStatus(postId, status);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái bài viết thành công", null));
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<Void>> moderateDeletePost(@PathVariable("postId") UUID postId) {
        adminService.moderateDeletePost(postId);
        return ResponseEntity.ok(ApiResponse.success("Xóa bài viết thành công", null));
    }

    @GetMapping("/reports/stores")
    public ResponseEntity<ApiResponse<List<StoreReportItem>>> getStoreReports() {
        List<StoreReportItem> response = adminService.getStoreReports();
        return ResponseEntity.ok(ApiResponse.success("Lấy báo cáo chi tiết cửa hàng thành công", response));
    }

    @GetMapping("/reports/customers")
    public ResponseEntity<ApiResponse<List<CustomerReportItem>>> getCustomerReports() {
        List<CustomerReportItem> response = adminService.getCustomerReports();
        return ResponseEntity.ok(ApiResponse.success("Lấy báo cáo chi tiết khách hàng thành công", response));
    }

    @GetMapping("/reports/stores/csv")
    public ResponseEntity<byte[]> exportStoreReportsToCsv() {
        String csv = adminService.exportStoreReportsToCsv();
        byte[] bytes = csv.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=stores_report.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(bytes);
    }

    @GetMapping("/reports/customers/csv")
    public ResponseEntity<byte[]> exportCustomerReportsToCsv() {
        String csv = adminService.exportCustomerReportsToCsv();
        byte[] bytes = csv.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=customers_report.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(bytes);
    }
}
