package com.urmyfood.backend.infrastructure.persistence.adapter;

import com.urmyfood.backend.domain.model.ShopProfile;
import com.urmyfood.backend.domain.model.Account;
import com.urmyfood.backend.domain.repository.ShopProfileRepository;
import com.urmyfood.backend.infrastructure.persistence.entity.AccountEntity;
import com.urmyfood.backend.infrastructure.persistence.entity.ShopProfileEntity;
import com.urmyfood.backend.infrastructure.persistence.repository.JpaAccountRepository;
import com.urmyfood.backend.infrastructure.persistence.repository.JpaShopProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ShopProfilePersistenceAdapter implements ShopProfileRepository {

    private final JpaShopProfileRepository jpaShopProfileRepository;
    private final JpaAccountRepository jpaAccountRepository;

    @Override
    public ShopProfile save(ShopProfile profile) {
        return toDomain(jpaShopProfileRepository.save(toEntity(profile)));
    }

    @Override
    public Optional<ShopProfile> findByShopId(Long shopId) {
        return jpaShopProfileRepository.findByShopId(shopId).map(this::toDomain);
    }

    private ShopProfileEntity toEntity(ShopProfile profile) {
        AccountEntity shop = jpaAccountRepository.findById(profile.getShop().getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản chủ quán"));

        return ShopProfileEntity.builder()
                .id(profile.getId())
                .shop(shop)
                .shopName(profile.getShopName())
                .logoUrl(profile.getLogoUrl())
                .coverUrl(profile.getCoverUrl())
                .category(profile.getCategory())
                .address(profile.getAddress())
                .latitude(profile.getLatitude())
                .longitude(profile.getLongitude())
                .description(profile.getDescription())
                .openingHours(profile.getOpeningHours())
                .isOpen(Boolean.TRUE.equals(profile.getIsOpen()))
                .build();
    }

    private ShopProfile toDomain(ShopProfileEntity entity) {
        return ShopProfile.builder()
                .id(entity.getId())
                .shop(Account.builder().id(entity.getShop().getId()).build())
                .shopName(entity.getShopName())
                .logoUrl(entity.getLogoUrl())
                .coverUrl(entity.getCoverUrl())
                .category(entity.getCategory())
                .address(entity.getAddress())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .description(entity.getDescription())
                .openingHours(entity.getOpeningHours())
                .isOpen(entity.getIsOpen())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
