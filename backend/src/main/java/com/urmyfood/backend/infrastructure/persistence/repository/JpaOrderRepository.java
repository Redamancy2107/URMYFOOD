package com.urmyfood.backend.infrastructure.persistence.repository;

import com.urmyfood.backend.infrastructure.persistence.entity.OrderEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaOrderRepository extends JpaRepository<OrderEntity, UUID> {
    List<OrderEntity> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM OrderEntity o WHERE o.orderId = :orderId")
    Optional<OrderEntity> findByIdForUpdate(@Param("orderId") UUID orderId);

    @Query("""
            SELECT o FROM OrderEntity o
            WHERE o.shop.id = :shopId
              AND o.createdAt >= :startAt
              AND o.createdAt < :endAt
            ORDER BY o.createdAt ASC
            """)
    List<OrderEntity> findShopOrdersForStatistics(
            @Param("shopId") Long shopId,
            @Param("startAt") OffsetDateTime startAt,
            @Param("endAt") OffsetDateTime endAt
    );

    @Query("""
            SELECT o FROM OrderEntity o
            WHERE o.shop.id = :shopId
            ORDER BY o.createdAt ASC
            """)
    List<OrderEntity> findShopOrdersForAllTimeStatistics(@Param("shopId") Long shopId);
}
