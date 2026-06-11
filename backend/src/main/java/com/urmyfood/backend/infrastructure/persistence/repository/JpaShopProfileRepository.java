package com.urmyfood.backend.infrastructure.persistence.repository;

import com.urmyfood.backend.infrastructure.persistence.entity.ShopProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaShopProfileRepository extends JpaRepository<ShopProfileEntity, Long> {
    Optional<ShopProfileEntity> findByShopId(Long shopId);
}
