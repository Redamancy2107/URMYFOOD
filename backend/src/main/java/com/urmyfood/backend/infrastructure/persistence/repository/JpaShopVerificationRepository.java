package com.urmyfood.backend.infrastructure.persistence.repository;

import com.urmyfood.backend.infrastructure.persistence.entity.ShopVerificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaShopVerificationRepository extends JpaRepository<ShopVerificationEntity, Long> {
    Optional<ShopVerificationEntity> findByShopId(Long shopId);
}
