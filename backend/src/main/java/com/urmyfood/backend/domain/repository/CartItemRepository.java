package com.urmyfood.backend.domain.repository;

import com.urmyfood.backend.domain.model.CartItem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CartItemRepository {
    CartItem save(CartItem cartItem);
    List<CartItem> findByCustomerId(Long customerId);
    Optional<CartItem> findById(UUID cartItemId);
    Optional<CartItem> findByCustomerIdAndPostId(Long customerId, UUID postId);
    void deleteById(UUID cartItemId);
    void deleteByCustomerId(Long customerId);
}
