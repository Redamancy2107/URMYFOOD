package com.urmyfood.backend.domain.repository;

import com.urmyfood.backend.domain.model.OrderReview;

import java.util.Optional;
import java.util.UUID;

public interface OrderReviewRepository {
    Optional<OrderReview> findByOrderId(UUID orderId);
    Optional<OrderReview> findByOrderIdAndCustomerId(UUID orderId, Long customerId);
    boolean existsByOrderId(UUID orderId);
    OrderReview save(OrderReview review);
}
