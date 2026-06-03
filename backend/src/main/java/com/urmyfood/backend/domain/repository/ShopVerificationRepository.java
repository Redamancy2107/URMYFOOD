package com.urmyfood.backend.domain.repository;

import com.urmyfood.backend.domain.model.ShopVerification;

import java.util.Optional;

public interface ShopVerificationRepository {
    ShopVerification save(ShopVerification verification);
    Optional<ShopVerification> findByShopId(Long shopId);
}
