package com.urmyfood.backend.domain.repository;

import com.urmyfood.backend.domain.model.ShopVerification;

import java.util.List;
import java.util.Optional;

public interface ShopVerificationRepository {
    ShopVerification save(ShopVerification verification);
    Optional<ShopVerification> findByShopId(Long shopId);
    List<ShopVerification> findPending();
    Optional<ShopVerification> findById(Long id);
}
