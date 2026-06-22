package com.urmyfood.backend.application.service;

import com.urmyfood.backend.application.dto.ProfileImageUploadResponse;
import com.urmyfood.backend.application.dto.ShopProfileRequest;
import com.urmyfood.backend.application.dto.ShopProfileResponse;
import com.urmyfood.backend.domain.model.Account;
import com.urmyfood.backend.domain.model.ShopProfile;
import com.urmyfood.backend.domain.model.ShopProfileImageType;
import com.urmyfood.backend.domain.model.ShopVerification;
import com.urmyfood.backend.domain.repository.AccountRepository;
import com.urmyfood.backend.domain.repository.ShopFollowRepository;
import com.urmyfood.backend.domain.repository.ShopProfileRepository;
import com.urmyfood.backend.domain.repository.ShopVerificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShopProfileService {

    private static final String ROLE_SHOP = "SHOP";
    private static final String DEFAULT_CATEGORY = "KHAC";
    private static final String DEFAULT_ADDRESS = "Chưa cập nhật địa chỉ";
    private static final String DEFAULT_OPENING_HOURS = "08:00 - 22:00";
    private static final int MAX_DESCRIPTION_LENGTH = 300;
    private static final Set<String> ALLOWED_CATEGORIES = Set.of(
            "COM", "PHO", "BUN", "BANH_MI", "DO_UONG", "NUONG", "CHAY", "KHAC"
    );

    private final AccountRepository accountRepository;
    private final ShopProfileRepository shopProfileRepository;
    private final ShopVerificationRepository shopVerificationRepository;
    private final ShopFollowRepository shopFollowRepository;
    private final ProfileImageStorageClient profileImageStorageClient;

    @Transactional(readOnly = true)
    public ShopProfileResponse getMyProfile(Long shopId) {
        Account shop = requireShopAccount(shopId);
        ShopVerification verification = shopVerificationRepository.findByShopId(shopId).orElse(null);
        ShopProfile profile = shopProfileRepository.findByShopId(shopId)
                .orElseGet(() -> buildFallbackProfile(shop, verification));
        profile.setVerificationStatus(statusOf(verification));
        return toResponse(profile, false, shopFollowRepository.countByShopId(shopId));
    }

    @Transactional(readOnly = true)
    public ShopProfileResponse getPublicProfile(Long shopId) {
        Account shop = requireShopAccount(shopId);
        ShopVerification verification = shopVerificationRepository.findByShopId(shopId).orElse(null);
        ShopProfile profile = shopProfileRepository.findByShopId(shopId)
                .orElseGet(() -> buildFallbackProfile(shop, verification));
        profile.setVerificationStatus(statusOf(verification));
        return toResponse(profile, false, shopFollowRepository.countByShopId(shopId));
    }

    @Transactional(readOnly = true)
    public ShopProfileResponse getProfileForViewer(Long shopId, Long viewerId) {
        Account shop = requireShopAccount(shopId);
        ShopVerification verification = shopVerificationRepository.findByShopId(shopId).orElse(null);
        ShopProfile profile = shopProfileRepository.findByShopId(shopId)
                .orElseGet(() -> buildFallbackProfile(shop, verification));
        profile.setVerificationStatus(statusOf(verification));
        boolean following = shopFollowRepository.isFollowing(viewerId, shopId);
        long followerCount = shopFollowRepository.countByShopId(shopId);
        return toResponse(profile, following, followerCount);
    }

    @Transactional
    public ShopProfileResponse updateMyProfile(Long shopId, ShopProfileRequest request) {
        log.info(
                "Updating shop profile. shopId={}, hasLogo={}, hasCover={}",
                shopId,
                trimToNull(request.getLogoUrl()) != null,
                trimToNull(request.getCoverUrl()) != null
        );
        Account shop = requireShopAccount(shopId);
        ShopVerification verification = shopVerificationRepository.findByShopId(shopId).orElse(null);
        Optional<ShopProfile> existingProfile = shopProfileRepository.findByShopId(shopId);
        ShopProfile existing = existingProfile.orElseGet(() -> buildFallbackProfile(shop, verification));
        String oldLogoUrl = existingProfile.map(ShopProfile::getLogoUrl).orElse(null);
        String oldCoverUrl = existingProfile.map(ShopProfile::getCoverUrl).orElse(null);

        String shopName = requireText(request.getShopName(), "Tên quán không được để trống");
        String category = requireCategory(request.getCategory());
        String address = requireText(request.getAddress(), "Địa chỉ quán không được để trống");
        String description = trimToNull(request.getDescription());
        validateCoordinates(request.getLatitude(), request.getLongitude());
        if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new IllegalArgumentException("Mô tả quán không được vượt quá 300 ký tự");
        }

        ShopProfile profile = ShopProfile.builder()
                .id(existing.getId())
                .shop(shop)
                .shopName(shopName)
                .logoUrl(trimToNull(request.getLogoUrl()))
                .coverUrl(trimToNull(request.getCoverUrl()))
                .category(category)
                .address(address)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .description(description)
                .openingHours(defaultIfBlank(request.getOpeningHours(), DEFAULT_OPENING_HOURS))
                .isOpen(request.getIsOpen() == null ? Boolean.TRUE : request.getIsOpen())
                .build();

        ShopProfile saved = shopProfileRepository.save(profile);
        shop.setFullName(shopName);
        shop.setAvatarUrl(saved.getLogoUrl());
        accountRepository.save(shop);
        deleteReplacedProfileImages(shopId, oldLogoUrl, saved.getLogoUrl(), oldCoverUrl, saved.getCoverUrl());
        log.info("Updated shop profile successfully. shopId={}, profileId={}", shopId, saved.getId());
        saved.setVerificationStatus(statusOf(verification));
        return toResponse(saved, false, shopFollowRepository.countByShopId(shopId));
    }

    public ProfileImageUploadResponse uploadProfileImage(Long shopId, ShopProfileImageType type, MultipartFile file) {
        requireShopAccount(shopId);
        if (type == null) {
            throw new IllegalArgumentException("Loại ảnh hồ sơ không hợp lệ");
        }
        String imageUrl = profileImageStorageClient.uploadShopProfileImage(shopId, type, file);
        return ProfileImageUploadResponse.builder()
                .imageUrl(imageUrl)
                .build();
    }

    private void deleteReplacedProfileImages(Long shopId, String oldLogoUrl, String newLogoUrl, String oldCoverUrl, String newCoverUrl) {
        deleteReplacedProfileImage(shopId, oldLogoUrl, newLogoUrl);
        deleteReplacedProfileImage(shopId, oldCoverUrl, newCoverUrl);
    }

    private void deleteReplacedProfileImage(Long shopId, String oldUrl, String newUrl) {
        if (oldUrl != null && !Objects.equals(oldUrl, newUrl)) {
            profileImageStorageClient.deleteShopProfileImage(shopId, oldUrl);
        }
    }

    private Account requireShopAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));
        if (!ROLE_SHOP.equals(account.getRole())) {
            throw new AccessDeniedException("Chỉ tài khoản chủ quán mới được dùng chức năng này");
        }
        return account;
    }

    private ShopProfile buildFallbackProfile(Account shop, ShopVerification verification) {
        return ShopProfile.builder()
                .shop(shop)
                .shopName(defaultIfBlank(verification == null ? null : verification.getShopName(), shop.getFullName()))
                .logoUrl(shop.getAvatarUrl())
                .coverUrl(null)
                .category(defaultIfBlank(verification == null ? null : verification.getCategory(), DEFAULT_CATEGORY))
                .address(defaultIfBlank(verification == null ? null : verification.getAddress(), DEFAULT_ADDRESS))
                .latitude(verification == null ? null : verification.getLatitude())
                .longitude(verification == null ? null : verification.getLongitude())
                .description(null)
                .openingHours(DEFAULT_OPENING_HOURS)
                .isOpen(Boolean.TRUE)
                .build();
    }

    private ShopProfileResponse toResponse(ShopProfile profile, boolean isFollowing, long followerCount) {
        return ShopProfileResponse.builder()
                .id(profile.getId())
                .shopId(profile.getShop().getId())
                .shopName(profile.getShopName())
                .logoUrl(profile.getLogoUrl())
                .coverUrl(profile.getCoverUrl())
                .category(profile.getCategory())
                .address(profile.getAddress())
                .latitude(profile.getLatitude())
                .longitude(profile.getLongitude())
                .description(profile.getDescription())
                .openingHours(profile.getOpeningHours())
                .isOpen(profile.getIsOpen())
                .verificationStatus(profile.getVerificationStatus())
                .isFollowing(isFollowing)
                .followerCount(followerCount)
                .build();
    }

    private String statusOf(ShopVerification verification) {
        return verification == null ? "NOT_SUBMITTED" : verification.getStatus().name();
    }

    private String requireText(String value, String message) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            throw new IllegalArgumentException(message);
        }
        return trimmed;
    }

    private String requireCategory(String value) {
        String category = requireText(value, "Danh mục quán không được để trống");
        if (!ALLOWED_CATEGORIES.contains(category)) {
            throw new IllegalArgumentException("Danh mục quán không hợp lệ");
        }
        return category;
    }

    private void validateCoordinates(Double latitude, Double longitude) {
        if ((latitude == null) != (longitude == null)) {
            throw new IllegalArgumentException("Vui lòng cung cấp đầy đủ tọa độ quán");
        }
        if (latitude != null && (latitude < -90 || latitude > 90)) {
            throw new IllegalArgumentException("Vĩ độ quán không hợp lệ");
        }
        if (longitude != null && (longitude < -180 || longitude > 180)) {
            throw new IllegalArgumentException("Kinh độ quán không hợp lệ");
        }
    }

    private String defaultIfBlank(String value, String fallback) {
        String trimmed = trimToNull(value);
        return trimmed == null ? fallback : trimmed;
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
