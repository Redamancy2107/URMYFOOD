package com.urmyfood.backend.presentation.controller;

import com.urmyfood.backend.application.dto.ApiResponse;
import com.urmyfood.backend.application.dto.ProfileImageUploadResponse;
import com.urmyfood.backend.application.dto.ShopFollowResponse;
import com.urmyfood.backend.application.dto.ShopProfileRequest;
import com.urmyfood.backend.application.dto.ShopProfileResponse;
import com.urmyfood.backend.application.dto.ShopStatisticsResponse;
import com.urmyfood.backend.application.dto.ShopVerificationRequest;
import com.urmyfood.backend.application.dto.ShopVerificationResponse;
import com.urmyfood.backend.application.service.ShopFollowService;
import com.urmyfood.backend.application.service.ShopProfileService;
import com.urmyfood.backend.application.service.ShopStatisticsService;
import com.urmyfood.backend.application.service.ShopVerificationService;
import com.urmyfood.backend.domain.model.ShopProfileImageType;
import com.urmyfood.backend.domain.model.ShopStatisticsPeriod;
import com.urmyfood.backend.infrastructure.security.CustomAccountDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/shops")
@RequiredArgsConstructor
public class ShopController {

    private final ShopVerificationService shopVerificationService;
    private final ShopProfileService shopProfileService;
    private final ShopStatisticsService shopStatisticsService;
    private final ShopFollowService shopFollowService;

    @GetMapping("/me/profile")
    public ResponseEntity<ApiResponse<ShopProfileResponse>> getMyProfile(Authentication authentication) {
        ShopProfileResponse response = shopProfileService.getMyProfile(getAccountId(authentication));
        return ResponseEntity.ok(ApiResponse.success("Lấy hồ sơ quán thành công", response));
    }

    @GetMapping("/{shopId}/profile/public")
    public ResponseEntity<ApiResponse<ShopProfileResponse>> getPublicProfile(@PathVariable Long shopId) {
        ShopProfileResponse response = shopProfileService.getPublicProfile(shopId);
        return ResponseEntity.ok(ApiResponse.success("Lấy hồ sơ quán thành công", response));
    }

    @GetMapping("/{shopId}/profile")
    public ResponseEntity<ApiResponse<ShopProfileResponse>> getProfile(
            Authentication authentication,
            @PathVariable Long shopId
    ) {
        ShopProfileResponse response = shopProfileService.getProfileForViewer(shopId, getAccountId(authentication));
        return ResponseEntity.ok(ApiResponse.success("Lay ho so quan thanh cong", response));
    }

    @GetMapping("/{shopId}/follow")
    public ResponseEntity<ApiResponse<ShopFollowResponse>> getFollowState(
            Authentication authentication,
            @PathVariable Long shopId
    ) {
        ShopFollowResponse response = shopFollowService.getFollowState(getAccountId(authentication), shopId);
        return ResponseEntity.ok(ApiResponse.success("Lay trang thai theo doi shop thanh cong", response));
    }

    @PostMapping("/{shopId}/follow")
    public ResponseEntity<ApiResponse<ShopFollowResponse>> followShop(
            Authentication authentication,
            @PathVariable Long shopId
    ) {
        ShopFollowResponse response = shopFollowService.follow(getAccountId(authentication), shopId);
        return ResponseEntity.ok(ApiResponse.success("Theo doi shop thanh cong", response));
    }

    @DeleteMapping("/{shopId}/follow")
    public ResponseEntity<ApiResponse<ShopFollowResponse>> unfollowShop(
            Authentication authentication,
            @PathVariable Long shopId
    ) {
        ShopFollowResponse response = shopFollowService.unfollow(getAccountId(authentication), shopId);
        return ResponseEntity.ok(ApiResponse.success("Bo theo doi shop thanh cong", response));
    }

    @PutMapping("/me/profile")
    public ResponseEntity<ApiResponse<ShopProfileResponse>> updateMyProfile(
            Authentication authentication,
            @RequestBody ShopProfileRequest request
    ) {
        ShopProfileResponse response = shopProfileService.updateMyProfile(getAccountId(authentication), request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật hồ sơ quán thành công", response));
    }

    @PostMapping("/me/profile/images")
    public ResponseEntity<ApiResponse<ProfileImageUploadResponse>> uploadProfileImage(
            Authentication authentication,
            @RequestParam ShopProfileImageType type,
            @RequestParam("file") MultipartFile file
    ) {
        ProfileImageUploadResponse response = shopProfileService.uploadProfileImage(getAccountId(authentication), type, file);
        return ResponseEntity.ok(ApiResponse.success("Tải ảnh hồ sơ thành công", response));
    }

    @GetMapping("/me/statistics")
    public ResponseEntity<ApiResponse<ShopStatisticsResponse>> getMyStatistics(
            Authentication authentication,
            @RequestParam ShopStatisticsPeriod period,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String year
    ) {
        ShopStatisticsResponse response = shopStatisticsService.getStatistics(
                getAccountId(authentication),
                period,
                date,
                month,
                year
        );
        return ResponseEntity.ok(ApiResponse.success("Lấy thống kê cửa hàng thành công", response));
    }

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
