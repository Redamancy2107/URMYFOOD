package com.urmyfood.backend.domain.repository;

import com.urmyfood.backend.domain.model.Order;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {
    Order save(Order order);
    List<Order> findByCustomerId(Long customerId);
    List<Order> findByShopId(Long shopId);
    List<Order> findShopOrdersForStatistics(Long shopId, OffsetDateTime startAt, OffsetDateTime endAt);
    List<Order> findShopOrdersForAllTimeStatistics(Long shopId);
    List<Order> findPendingOrAcceptedOrdersByPostId(UUID postId);
    List<Order> findPendingOrAcceptedOrdersByShopId(Long shopId);
    Optional<Order> findById(UUID orderId);
    Optional<Order> findByIdForUpdate(UUID orderId);
    Optional<Order> findByPayosOrderCode(Long payosOrderCode);
    List<Order> findPendingExpiredOrders(OffsetDateTime expiredBefore);
    List<Order> findAcceptedUnpaidExpiredOrders(OffsetDateTime expiredBefore);
}
