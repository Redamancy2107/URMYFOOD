package com.urmyfood.backend.infrastructure.persistence.adapter;

import com.urmyfood.backend.domain.model.Account;
import com.urmyfood.backend.domain.model.ShopVerification;
import com.urmyfood.backend.domain.repository.ShopVerificationRepository;
import com.urmyfood.backend.infrastructure.persistence.entity.AccountEntity;
import com.urmyfood.backend.infrastructure.persistence.entity.ShopVerificationEntity;
import com.urmyfood.backend.infrastructure.persistence.repository.JpaAccountRepository;
import com.urmyfood.backend.infrastructure.persistence.repository.JpaShopVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ShopVerificationPersistenceAdapter implements ShopVerificationRepository {

    private final JpaShopVerificationRepository jpaShopVerificationRepository;
    private final JpaAccountRepository jpaAccountRepository;

    @Override
    public ShopVerification save(ShopVerification verification) {
        return toDomain(jpaShopVerificationRepository.save(toEntity(verification)));
    }

    @Override
    public Optional<ShopVerification> findByShopId(Long shopId) {
        return jpaShopVerificationRepository.findByShopId(shopId).map(this::toDomain);
    }

    private ShopVerificationEntity toEntity(ShopVerification verification) {
        AccountEntity shop = jpaAccountRepository.findById(verification.getShop().getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản chủ quán"));

        return ShopVerificationEntity.builder()
                .id(verification.getId())
                .shop(shop)
                .shopName(verification.getShopName())
                .category(verification.getCategory())
                .address(verification.getAddress())
                .latitude(verification.getLatitude())
                .longitude(verification.getLongitude())
                .cccdFrontUrl(verification.getCccdFrontUrl())
                .cccdBackUrl(verification.getCccdBackUrl())
                .shopPhotoUrls(encodePhotoUrls(verification.getShopPhotoUrls()))
                .status(verification.getStatus())
                .rejectReason(verification.getRejectReason())
                .build();
    }

    private ShopVerification toDomain(ShopVerificationEntity entity) {
        return ShopVerification.builder()
                .id(entity.getId())
                .shop(Account.builder().id(entity.getShop().getId()).build())
                .shopName(entity.getShopName())
                .category(entity.getCategory())
                .address(entity.getAddress())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .cccdFrontUrl(entity.getCccdFrontUrl())
                .cccdBackUrl(entity.getCccdBackUrl())
                .shopPhotoUrls(decodePhotoUrls(entity.getShopPhotoUrls()))
                .status(entity.getStatus())
                .rejectReason(entity.getRejectReason())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private String encodePhotoUrls(List<String> urls) {
        return String.join("\n", urls == null ? List.of() : urls);
    }

    private List<String> decodePhotoUrls(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return Arrays.stream(raw.split("\\R"))
                .filter(value -> !value.isBlank())
                .toList();
    }
}
