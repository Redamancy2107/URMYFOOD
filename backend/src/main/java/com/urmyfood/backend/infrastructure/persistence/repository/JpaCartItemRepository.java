package com.urmyfood.backend.infrastructure.persistence.repository;

import com.urmyfood.backend.infrastructure.persistence.entity.CartItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaCartItemRepository extends JpaRepository<CartItemEntity, UUID> {

    @Query("SELECT ci FROM CartItemEntity ci JOIN FETCH ci.post p JOIN FETCH p.author JOIN FETCH ci.customer WHERE ci.customer.id = :customerId ORDER BY ci.createdAt DESC")
    List<CartItemEntity> findByCustomerIdOrderByCreatedAtDesc(@Param("customerId") Long customerId);

    @Query("SELECT ci FROM CartItemEntity ci JOIN FETCH ci.post p JOIN FETCH p.author JOIN FETCH ci.customer WHERE ci.customer.id = :customerId AND p.postId = :postId")
    Optional<CartItemEntity> findByCustomerIdAndPostPostId(@Param("customerId") Long customerId, @Param("postId") UUID postId);

    @Query("SELECT ci FROM CartItemEntity ci JOIN FETCH ci.post p JOIN FETCH p.author JOIN FETCH ci.customer WHERE ci.cartItemId = :id")
    Optional<CartItemEntity> findById(@Param("id") UUID id);

    void deleteByCustomerId(Long customerId);
}
