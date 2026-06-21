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
    @Query("SELECT DISTINCT o FROM OrderEntity o " +
           "JOIN FETCH o.customer " +
           "JOIN FETCH o.shop " +
           "LEFT JOIN FETCH o.voucher " +
           "LEFT JOIN FETCH o.items i " +
           "LEFT JOIN FETCH i.post p " +
           "LEFT JOIN FETCH p.author " +
           "WHERE o.payosOrderCode = :payosOrderCode")
    Optional<OrderEntity> findByPayosOrderCode(@Param("payosOrderCode") Long payosOrderCode);
    @Query("SELECT DISTINCT o FROM OrderEntity o " +
           "JOIN FETCH o.customer " +
           "JOIN FETCH o.shop " +
           "LEFT JOIN FETCH o.voucher " +
           "LEFT JOIN FETCH o.items i " +
           "LEFT JOIN FETCH i.post p " +
           "LEFT JOIN FETCH p.author " +
           "WHERE o.orderId = :orderId")
    Optional<OrderEntity> findByIdWithDetails(@Param("orderId") UUID orderId);

    @Query("SELECT DISTINCT o FROM OrderEntity o " +
           "JOIN FETCH o.customer " +
           "JOIN FETCH o.shop " +
           "LEFT JOIN FETCH o.voucher " +
           "LEFT JOIN FETCH o.items i " +
           "LEFT JOIN FETCH i.post p " +
           "LEFT JOIN FETCH p.author " +
           "WHERE o.customer.id = :customerId ORDER BY o.createdAt DESC")
    List<OrderEntity> findByCustomerIdOrderByCreatedAtDesc(@Param("customerId") Long customerId);

    @Query("SELECT DISTINCT o FROM OrderEntity o " +
           "JOIN FETCH o.customer " +
           "JOIN FETCH o.shop " +
           "LEFT JOIN FETCH o.voucher " +
           "LEFT JOIN FETCH o.items i " +
           "LEFT JOIN FETCH i.post p " +
           "LEFT JOIN FETCH p.author " +
           "WHERE o.shop.id = :shopId ORDER BY o.createdAt DESC")
    List<OrderEntity> findByShopIdOrderByCreatedAtDesc(@Param("shopId") Long shopId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT DISTINCT o FROM OrderEntity o " +
           "JOIN FETCH o.customer " +
           "JOIN FETCH o.shop " +
           "LEFT JOIN FETCH o.voucher " +
           "LEFT JOIN FETCH o.items i " +
           "LEFT JOIN FETCH i.post p " +
           "LEFT JOIN FETCH p.author " +
           "WHERE o.orderId = :orderId")
    Optional<OrderEntity> findByIdForUpdate(@Param("orderId") UUID orderId);

    @Query("""
            SELECT DISTINCT o FROM OrderEntity o
            JOIN FETCH o.customer
            JOIN FETCH o.shop
            LEFT JOIN FETCH o.voucher
            LEFT JOIN FETCH o.items i
            LEFT JOIN FETCH i.post p
            LEFT JOIN FETCH p.author
            WHERE o.orderStatus = 'PENDING'
              AND o.paymentStatus = 'UNPAID'
              AND o.createdAt < :expiredBefore
            """)
    List<OrderEntity> findPendingExpiredOrders(@Param("expiredBefore") OffsetDateTime expiredBefore);

    @Query("""
            SELECT DISTINCT o FROM OrderEntity o
            JOIN FETCH o.customer
            JOIN FETCH o.shop
            LEFT JOIN FETCH o.voucher
            LEFT JOIN FETCH o.items i
            LEFT JOIN FETCH i.post p
            LEFT JOIN FETCH p.author
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
            SELECT DISTINCT o FROM OrderEntity o
            JOIN FETCH o.customer
            JOIN FETCH o.shop
            LEFT JOIN FETCH o.voucher
            LEFT JOIN FETCH o.items i
            LEFT JOIN FETCH i.post p
            LEFT JOIN FETCH p.author
            WHERE o.shop.id = :shopId
            ORDER BY o.createdAt ASC
            """)
    List<OrderEntity> findShopOrdersForAllTimeStatistics(@Param("shopId") Long shopId);
}
