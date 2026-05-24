package com.urmyfood.backend.infrastructure.persistence.repository;

import com.urmyfood.backend.infrastructure.persistence.entity.CartItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaCartItemRepository extends JpaRepository<CartItemEntity, UUID> {
    List<CartItemEntity> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    Optional<CartItemEntity> findByCustomerIdAndPostPostId(Long customerId, UUID postId);
    void deleteByCustomerId(Long customerId);
}
