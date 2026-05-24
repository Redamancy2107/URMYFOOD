package com.urmyfood.backend.application.service;

import com.urmyfood.backend.application.dto.AddCartItemRequest;
import com.urmyfood.backend.application.dto.CartItemResponse;
import com.urmyfood.backend.application.dto.CartResponse;
import com.urmyfood.backend.application.dto.UpdateCartItemRequest;
import com.urmyfood.backend.domain.model.Account;
import com.urmyfood.backend.domain.model.CartItem;
import com.urmyfood.backend.domain.model.Post;
import com.urmyfood.backend.domain.model.PostStatus;
import com.urmyfood.backend.domain.repository.AccountRepository;
import com.urmyfood.backend.domain.repository.CartItemRepository;
import com.urmyfood.backend.domain.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final PostRepository postRepository;
    private final AccountRepository accountRepository;

    public CartResponse getCart(Long customerId) {
        return toCartResponse(cartItemRepository.findByCustomerId(customerId));
    }

    @Transactional
    public CartResponse addItem(Long customerId, AddCartItemRequest request) {
        Account customer = accountRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));
        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy món ăn"));
        validateOrderablePost(post);

        List<CartItem> currentItems = cartItemRepository.findByCustomerId(customerId);
        validateSingleShopCart(currentItems, post);

        CartItem cartItem = cartItemRepository.findByCustomerIdAndPostId(customerId, request.getPostId())
                .orElse(CartItem.builder()
                        .customer(customer)
                        .post(post)
                        .quantity(0)
                        .build());

        int newQuantity = cartItem.getQuantity() + request.getQuantity();
        validateEnoughQuantity(post, newQuantity);
        cartItem.setQuantity(newQuantity);
        cartItemRepository.save(cartItem);

        return getCart(customerId);
    }

    @Transactional
    public CartResponse updateItem(Long customerId, UUID itemId, UpdateCartItemRequest request) {
        CartItem item = findOwnedCartItem(customerId, itemId);
        validateOrderablePost(item.getPost());
        validateEnoughQuantity(item.getPost(), request.getQuantity());

        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);
        return getCart(customerId);
    }

    @Transactional
    public void deleteItem(Long customerId, UUID itemId) {
        findOwnedCartItem(customerId, itemId);
        cartItemRepository.deleteById(itemId);
    }

    private CartItem findOwnedCartItem(Long customerId, UUID itemId) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy món trong giỏ hàng"));
        if (!item.getCustomer().getId().equals(customerId)) {
            throw new IllegalArgumentException("Bạn không có quyền thao tác món này");
        }
        return item;
    }

    private void validateSingleShopCart(List<CartItem> currentItems, Post post) {
        boolean hasOtherShop = currentItems.stream()
                .anyMatch(item -> !item.getPost().getAuthor().getId().equals(post.getAuthor().getId()));
        if (hasOtherShop) {
            throw new IllegalArgumentException("Một giỏ hàng chỉ được chứa món từ một chủ quán");
        }
    }

    private void validateOrderablePost(Post post) {
        if (post.getStatus() != PostStatus.ACTIVE) {
            throw new IllegalArgumentException("Món ăn hiện không thể đặt");
        }
        if (post.getRemainingQuantity() <= 0) {
            throw new IllegalArgumentException("Món ăn đã hết hàng");
        }
    }

    private void validateEnoughQuantity(Post post, int quantity) {
        if (quantity > post.getRemainingQuantity()) {
            throw new IllegalArgumentException("Số lượng trong giỏ vượt quá số lượng còn lại");
        }
    }

    private CartResponse toCartResponse(List<CartItem> items) {
        List<CartItemResponse> itemResponses = items.stream()
                .map(this::toItemResponse)
                .toList();
        BigDecimal totalAmount = itemResponses.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int itemCount = items.stream().mapToInt(CartItem::getQuantity).sum();

        return CartResponse.builder()
                .items(itemResponses)
                .totalAmount(totalAmount)
                .itemCount(itemCount)
                .build();
    }

    private CartItemResponse toItemResponse(CartItem item) {
        Post post = item.getPost();
        BigDecimal subtotal = post.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

        return CartItemResponse.builder()
                .cartItemId(item.getCartItemId())
                .postId(post.getPostId())
                .dishName(post.getDishName())
                .price(post.getPrice())
                .imageUrl(post.getImageUrl())
                .shopId(post.getAuthor().getId())
                .shopName(post.getAuthor().getFullName())
                .quantity(item.getQuantity())
                .subtotal(subtotal)
                .remainingQuantity(post.getRemainingQuantity())
                .build();
    }
}
