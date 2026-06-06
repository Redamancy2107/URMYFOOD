package com.urmyfood.backend.application.service;

import com.urmyfood.backend.application.dto.ShopVerificationRequest;
import com.urmyfood.backend.application.dto.ShopVerificationResponse;
import com.urmyfood.backend.domain.model.Account;
import com.urmyfood.backend.domain.model.ShopVerification;
import com.urmyfood.backend.domain.model.ShopVerificationStatus;
import com.urmyfood.backend.domain.repository.AccountRepository;
import com.urmyfood.backend.domain.repository.ShopVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShopVerificationService {

    private static final String ROLE_SHOP = "SHOP";

    private final AccountRepository accountRepository;
    private final ShopVerificationRepository shopVerificationRepository;

    public ShopVerificationResponse submit(Long shopId, ShopVerificationRequest request) {
        Account shop = requireShopAccount(shopId);
        ShopVerification existing = shopVerificationRepository.findByShopId(shopId).orElse(null);

        ShopVerification verification = ShopVerification.builder()
                .id(existing == null ? null : existing.getId())
                .shop(shop)
                .shopName(request.getShopName().trim())
                .category(request.getCategory().trim())
                .address(request.getAddress().trim())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .cccdFrontUrl(request.getCccdFrontUrl().trim())
                .cccdBackUrl(request.getCccdBackUrl().trim())
                .shopPhotoUrls(request.getShopPhotoUrls().stream().map(String::trim).filter(value -> !value.isBlank()).toList())
                .status(ShopVerificationStatus.PENDING)
                .rejectReason(null)
                .build();

        if (verification.getShopPhotoUrls().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng thêm ít nhất 1 ảnh quán");
        }

        return toResponse(shopVerificationRepository.save(verification));
    }

    public ShopVerificationResponse getMyVerification(Long shopId) {
        requireShopAccount(shopId);
        return shopVerificationRepository.findByShopId(shopId)
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Chưa có hồ sơ xác minh quán"));
    }

    private Account requireShopAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));
        if (!ROLE_SHOP.equals(account.getRole())) {
            throw new org.springframework.security.access.AccessDeniedException("Chỉ tài khoản chủ quán mới được dùng chức năng này");
        }
        return account;
    }

    private ShopVerificationResponse toResponse(ShopVerification verification) {
        return ShopVerificationResponse.builder()
                .id(verification.getId())
                .shopId(verification.getShop().getId())
                .shopName(verification.getShopName())
                .category(verification.getCategory())
                .address(verification.getAddress())
                .latitude(verification.getLatitude())
                .longitude(verification.getLongitude())
                .cccdFrontUrl(verification.getCccdFrontUrl())
                .cccdBackUrl(verification.getCccdBackUrl())
                .shopPhotoUrls(verification.getShopPhotoUrls())
                .status(verification.getStatus().name())
                .rejectReason(verification.getRejectReason())
                .createdAt(verification.getCreatedAt())
                .updatedAt(verification.getUpdatedAt())
                .build();
    }
}
