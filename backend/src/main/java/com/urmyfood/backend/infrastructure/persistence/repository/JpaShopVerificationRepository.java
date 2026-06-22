package com.urmyfood.backend.infrastructure.persistence.repository;

import com.urmyfood.backend.infrastructure.persistence.entity.ShopVerificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import com.urmyfood.backend.domain.model.ShopVerificationStatus;
import java.util.List;
import java.util.Optional;

public interface JpaShopVerificationRepository extends JpaRepository<ShopVerificationEntity, Long> {
    Optional<ShopVerificationEntity> findByShopId(Long shopId);
    List<ShopVerificationEntity> findByStatus(ShopVerificationStatus status);
}
