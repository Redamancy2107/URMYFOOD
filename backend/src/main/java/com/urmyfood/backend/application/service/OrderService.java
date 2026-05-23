package com.urmyfood.backend.application.service;

import com.urmyfood.backend.application.dto.CancelOrderRequest;
import com.urmyfood.backend.application.dto.CheckoutRequest;
import com.urmyfood.backend.application.dto.OrderItemResponse;
import com.urmyfood.backend.application.dto.OrderResponse;
import com.urmyfood.backend.domain.model.Account;
import com.urmyfood.backend.domain.model.CartItem;
import com.urmyfood.backend.domain.model.Order;
import com.urmyfood.backend.domain.model.OrderItem;
import com.urmyfood.backend.domain.model.OrderStatus;
import com.urmyfood.backend.domain.model.PaymentMethod;
import com.urmyfood.backend.domain.model.PaymentStatus;
import com.urmyfood.backend.domain.model.Post;
import com.urmyfood.backend.domain.model.PostStatus;
import com.urmyfood.backend.domain.model.Voucher;
import com.urmyfood.backend.domain.repository.AccountRepository;
import com.urmyfood.backend.domain.repository.CartItemRepository;
import com.urmyfood.backend.domain.repository.OrderRepository;
import com.urmyfood.backend.domain.repository.PostRepository;
import com.urmyfood.backend.domain.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final PostRepository postRepository;
    private final AccountRepository accountRepository;
    private final VoucherRepository voucherRepository;

    @Transactional
    public OrderResponse checkout(Long customerId, CheckoutRequest request) {
        Account customer = accountRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));
        List<CartItem> cartItems = cartItemRepository.findByCustomerId(customerId);
        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Giỏ hàng đang trống");
        }

        PaymentMethod paymentMethod = parsePaymentMethod(request.getPaymentMethod());
        Account shop = validateCartForCheckout(cartItems);
        BigDecimal totalAmount = calculateTotal(cartItems);
        Voucher voucher = resolveVoucher(request.getVoucherId(), totalAmount);
        BigDecimal discountAmount = voucher == null ? BigDecimal.ZERO : voucher.getDiscountValue().min(totalAmount);
        BigDecimal finalAmount = totalAmount.subtract(discountAmount).max(BigDecimal.ZERO);

        List<OrderItem> orderItems = cartItems.stream()
                .map(item -> toOrderItemSnapshot(item.getPost(), item.getQuantity()))
                .toList();

        Order order = Order.builder()
                .customer(customer)
                .shop(shop)
                .voucher(voucher)
                .totalAmount(totalAmount)
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .orderStatus(OrderStatus.PENDING)
                .paymentMethod(paymentMethod)
                .paymentStatus(PaymentStatus.UNPAID)
                .deliveryAddress(request.getDeliveryAddress())
                .note(request.getNote())
                .items(orderItems)
                .build();

        decreasePostQuantities(cartItems);
        Order savedOrder = orderRepository.save(order);
        cartItemRepository.deleteByCustomerId(customerId);

        return toResponse(savedOrder);
    }

    public List<OrderResponse> getMyOrders(Long customerId) {
        return orderRepository.findByCustomerId(customerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public OrderResponse getOrderDetail(Long customerId, UUID orderId) {
        Order order = findOwnedOrder(customerId, orderId);
        return toResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(Long customerId, UUID orderId, CancelOrderRequest request) {
        Order order = findOwnedOrder(customerId, orderId);
        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("Chỉ có thể hủy đơn hàng đang chờ xác nhận");
        }

        restorePostQuantities(order.getItems());
        order.setOrderStatus(OrderStatus.CANCELLED);
        order.setCancelReason(request.getCancelReason());
        return toResponse(orderRepository.save(order));
    }

    private Order findOwnedOrder(Long customerId, UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng"));
        if (!order.getCustomer().getId().equals(customerId)) {
            throw new IllegalArgumentException("Bạn không có quyền xem đơn hàng này");
        }
        return order;
    }

    private PaymentMethod parsePaymentMethod(String value) {
        try {
            return PaymentMethod.valueOf(value.trim().toUpperCase());
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Phương thức thanh toán không hợp lệ");
        }
    }

    private Account validateCartForCheckout(List<CartItem> cartItems) {
        Account shop = cartItems.get(0).getPost().getAuthor();
        for (CartItem item : cartItems) {
            Post post = item.getPost();
            if (!post.getAuthor().getId().equals(shop.getId())) {
                throw new IllegalArgumentException("Một đơn hàng không thể kết hợp món từ nhiều chủ quán");
            }
            if (post.getStatus() != PostStatus.ACTIVE) {
                throw new IllegalArgumentException("Món " + post.getDishName() + " hiện không thể đặt");
            }
            if (item.getQuantity() > post.getRemainingQuantity()) {
                throw new IllegalArgumentException("Món " + post.getDishName() + " không còn đủ số lượng");
            }
        }
        return shop;
    }

    private Voucher resolveVoucher(Long voucherId, BigDecimal totalAmount) {
        if (voucherId == null) {
            return null;
        }
        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy voucher"));
        if (!voucher.isActive() || voucher.getExpiryDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Voucher không còn hiệu lực");
        }
        if (totalAmount.compareTo(voucher.getMinOrderValue()) < 0) {
            throw new IllegalArgumentException("Đơn hàng chưa đạt giá trị tối thiểu để áp dụng voucher");
        }
        return voucher;
    }

    private BigDecimal calculateTotal(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(item -> item.getPost().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private OrderItem toOrderItemSnapshot(Post post, int quantity) {
        return OrderItem.builder()
                .post(post)
                .quantity(quantity)
                .priceAtPurchase(post.getPrice())
                .dishNameSnapshot(post.getDishName())
                .imageUrlSnapshot(post.getImageUrl())
                .build();
    }

    private void decreasePostQuantities(List<CartItem> cartItems) {
        cartItems.forEach(item -> {
            Post post = item.getPost();
            int remainingQuantity = post.getRemainingQuantity() - item.getQuantity();
            post.setRemainingQuantity(remainingQuantity);
            if (remainingQuantity == 0) {
                post.setStatus(PostStatus.SOLD_OUT);
            }
            postRepository.save(post);
        });
    }

    private void restorePostQuantities(List<OrderItem> orderItems) {
        orderItems.forEach(item -> {
            Post post = postRepository.findById(item.getPost().getPostId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy món ăn"));
            post.setRemainingQuantity(post.getRemainingQuantity() + item.getQuantity());
            if (post.getStatus() == PostStatus.SOLD_OUT && post.getRemainingQuantity() > 0) {
                post.setStatus(PostStatus.ACTIVE);
            }
            postRepository.save(post);
        });
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(this::toItemResponse)
                .toList();

        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .customerId(order.getCustomer().getId())
                .shopId(order.getShop().getId())
                .shopName(order.getShop().getFullName())
                .voucherId(order.getVoucher() == null ? null : order.getVoucher().getId())
                .totalAmount(order.getTotalAmount())
                .discountAmount(order.getDiscountAmount())
                .finalAmount(order.getFinalAmount())
                .orderStatus(order.getOrderStatus().name())
                .paymentMethod(order.getPaymentMethod().name())
                .paymentStatus(order.getPaymentStatus().name())
                .deliveryAddress(order.getDeliveryAddress())
                .note(order.getNote())
                .cancelReason(order.getCancelReason())
                .items(itemResponses)
                .createdAt(order.getCreatedAt())
                .build();
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        BigDecimal subtotal = item.getPriceAtPurchase().multiply(BigDecimal.valueOf(item.getQuantity()));
        return OrderItemResponse.builder()
                .orderItemId(item.getOrderItemId())
                .postId(item.getPost().getPostId())
                .dishName(item.getDishNameSnapshot())
                .imageUrl(item.getImageUrlSnapshot())
                .quantity(item.getQuantity())
                .priceAtPurchase(item.getPriceAtPurchase())
                .subtotal(subtotal)
                .build();
    }
}
