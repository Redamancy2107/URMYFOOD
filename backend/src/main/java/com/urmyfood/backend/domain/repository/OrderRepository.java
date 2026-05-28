package com.urmyfood.backend.domain.repository;

import com.urmyfood.backend.domain.model.Order;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {
    Order save(Order order);
    List<Order> findByCustomerId(Long customerId);
    Optional<Order> findById(UUID orderId);
    Optional<Order> findByIdForUpdate(UUID orderId);
}
