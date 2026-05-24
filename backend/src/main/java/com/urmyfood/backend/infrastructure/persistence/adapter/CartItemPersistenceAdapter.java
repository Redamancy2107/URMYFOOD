package com.urmyfood.backend.infrastructure.persistence.adapter;

import com.urmyfood.backend.domain.model.CartItem;
import com.urmyfood.backend.domain.repository.CartItemRepository;
import com.urmyfood.backend.infrastructure.persistence.entity.AccountEntity;
import com.urmyfood.backend.infrastructure.persistence.entity.CartItemEntity;
import com.urmyfood.backend.infrastructure.persistence.entity.PostEntity;
import com.urmyfood.backend.infrastructure.persistence.repository.JpaAccountRepository;
import com.urmyfood.backend.infrastructure.persistence.repository.JpaCartItemRepository;
import com.urmyfood.backend.infrastructure.persistence.repository.JpaPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CartItemPersistenceAdapter implements CartItemRepository {

    private final JpaCartItemRepository jpaCartItemRepository;
    private final JpaAccountRepository jpaAccountRepository;
    private final JpaPostRepository jpaPostRepository;
    private final AccountPersistenceAdapter accountAdapter;
    private final PostPersistenceAdapter postAdapter;

    @Override
    public CartItem save(CartItem cartItem) {
        return toDomain(jpaCartItemRepository.save(toEntity(cartItem)));
    }

    @Override
    public List<CartItem> findByCustomerId(Long customerId) {
        return jpaCartItemRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<CartItem> findById(UUID cartItemId) {
        return jpaCartItemRepository.findById(cartItemId).map(this::toDomain);
    }

    @Override
    public Optional<CartItem> findByCustomerIdAndPostId(Long customerId, UUID postId) {
        return jpaCartItemRepository.findByCustomerIdAndPostPostId(customerId, postId).map(this::toDomain);
    }

    @Override
    public void deleteById(UUID cartItemId) {
        jpaCartItemRepository.deleteById(cartItemId);
    }

    @Override
    public void deleteByCustomerId(Long customerId) {
        jpaCartItemRepository.deleteByCustomerId(customerId);
    }

    private CartItem toDomain(CartItemEntity entity) {
        return CartItem.builder()
                .cartItemId(entity.getCartItemId())
                .customer(accountAdapter.toDomain(entity.getCustomer()))
                .post(postAdapter.toDomain(entity.getPost()))
                .quantity(entity.getQuantity())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private CartItemEntity toEntity(CartItem cartItem) {
        AccountEntity customer = jpaAccountRepository.findById(cartItem.getCustomer().getId())
                .orElseThrow(() -> new RuntimeException("Customer account not found: " + cartItem.getCustomer().getId()));
        PostEntity post = jpaPostRepository.findById(cartItem.getPost().getPostId())
                .orElseThrow(() -> new RuntimeException("Post not found: " + cartItem.getPost().getPostId()));

        return CartItemEntity.builder()
                .cartItemId(cartItem.getCartItemId())
                .customer(customer)
                .post(post)
                .quantity(cartItem.getQuantity())
                .createdAt(cartItem.getCreatedAt())
                .updatedAt(cartItem.getUpdatedAt())
                .build();
    }
}
