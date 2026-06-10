package com.urmyfood.backend.domain.repository;

import com.urmyfood.backend.domain.model.ShopProfile;

import java.util.Optional;

public interface ShopProfileRepository {
    ShopProfile save(ShopProfile profile);
    Optional<ShopProfile> findByShopId(Long shopId);
}
