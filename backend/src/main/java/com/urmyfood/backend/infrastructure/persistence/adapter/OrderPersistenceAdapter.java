package com.urmyfood.backend.infrastructure.persistence.adapter;

import com.urmyfood.backend.domain.model.Order;
import com.urmyfood.backend.domain.model.OrderItem;
import com.urmyfood.backend.domain.repository.OrderRepository;
import com.urmyfood.backend.infrastructure.persistence.entity.AccountEntity;
import com.urmyfood.backend.infrastructure.persistence.entity.OrderEntity;
import com.urmyfood.backend.infrastructure.persistence.entity.OrderItemEntity;
import com.urmyfood.backend.infrastructure.persistence.entity.PostEntity;
import com.urmyfood.backend.infrastructure.persistence.entity.VoucherEntity;
import com.urmyfood.backend.infrastructure.persistence.repository.JpaAccountRepository;
import com.urmyfood.backend.infrastructure.persistence.repository.JpaOrderRepository;
import com.urmyfood.backend.infrastructure.persistence.repository.JpaPostRepository;
import com.urmyfood.backend.infrastructure.persistence.repository.JpaVoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderPersistenceAdapter implements OrderRepository {

    private final JpaOrderRepository jpaOrderRepository;
    private final JpaAccountRepository jpaAccountRepository;
    private final JpaPostRepository jpaPostRepository;
    private final JpaVoucherRepository jpaVoucherRepository;
    private final AccountPersistenceAdapter accountAdapter;
    private final PostPersistenceAdapter postAdapter;
    private final VoucherPersistenceAdapter voucherAdapter;

    @Override
    public Order save(Order order) {
        return toDomain(jpaOrderRepository.save(toEntity(order)));
    }

    @Override
    public List<Order> findByCustomerId(Long customerId) {
        return jpaOrderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Order> findById(UUID orderId) {
        return jpaOrderRepository.findById(orderId).map(this::toDomain);
    }

    @Override
    public Optional<Order> findByIdForUpdate(UUID orderId) {
        return jpaOrderRepository.findByIdForUpdate(orderId).map(this::toDomain);
    }

    private Order toDomain(OrderEntity entity) {
        List<OrderItem> items = entity.getItems().stream()
                .map(this::toDomainItem)
                .toList();

        return Order.builder()
                .orderId(entity.getOrderId())
                .customer(accountAdapter.toDomain(entity.getCustomer()))
                .shop(accountAdapter.toDomain(entity.getShop()))
                .voucher(entity.getVoucher() == null ? null : voucherAdapter.toDomain(entity.getVoucher()))
                .totalAmount(entity.getTotalAmount())
                .discountAmount(entity.getDiscountAmount())
                .finalAmount(entity.getFinalAmount())
                .orderStatus(entity.getOrderStatus())
                .paymentMethod(entity.getPaymentMethod())
                .paymentStatus(entity.getPaymentStatus())
                .deliveryAddress(entity.getDeliveryAddress())
                .note(entity.getNote())
                .cancelReason(entity.getCancelReason())
                .items(items)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private OrderItem toDomainItem(OrderItemEntity entity) {
        return OrderItem.builder()
                .orderItemId(entity.getOrderItemId())
                .orderId(entity.getOrder().getOrderId())
                .post(postAdapter.toDomain(entity.getPost()))
                .quantity(entity.getQuantity())
                .priceAtPurchase(entity.getPriceAtPurchase())
                .dishNameSnapshot(entity.getDishNameSnapshot())
                .imageUrlSnapshot(entity.getImageUrlSnapshot())
                .build();
    }

    private OrderEntity toEntity(Order order) {
        AccountEntity customer = jpaAccountRepository.findById(order.getCustomer().getId())
                .orElseThrow(() -> new RuntimeException("Customer account not found: " + order.getCustomer().getId()));
        AccountEntity shop = jpaAccountRepository.findById(order.getShop().getId())
                .orElseThrow(() -> new RuntimeException("Shop account not found: " + order.getShop().getId()));
        VoucherEntity voucher = null;
        if (order.getVoucher() != null) {
            voucher = jpaVoucherRepository.findById(order.getVoucher().getId())
                    .orElseThrow(() -> new RuntimeException("Voucher not found: " + order.getVoucher().getId()));
        }

        OrderEntity entity = OrderEntity.builder()
                .orderId(order.getOrderId())
                .customer(customer)
                .shop(shop)
                .voucher(voucher)
                .totalAmount(order.getTotalAmount())
                .discountAmount(order.getDiscountAmount())
                .finalAmount(order.getFinalAmount())
                .orderStatus(order.getOrderStatus())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .deliveryAddress(order.getDeliveryAddress())
                .note(order.getNote())
                .cancelReason(order.getCancelReason())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(new ArrayList<>())
                .build();

        if (order.getItems() != null) {
            order.getItems().forEach(item -> entity.getItems().add(toEntityItem(item, entity)));
        }
        return entity;
    }

    private OrderItemEntity toEntityItem(OrderItem item, OrderEntity order) {
        PostEntity post = jpaPostRepository.findById(item.getPost().getPostId())
                .orElseThrow(() -> new RuntimeException("Post not found: " + item.getPost().getPostId()));

        return OrderItemEntity.builder()
                .orderItemId(item.getOrderItemId())
                .order(order)
                .post(post)
                .quantity(item.getQuantity())
                .priceAtPurchase(item.getPriceAtPurchase())
                .dishNameSnapshot(item.getDishNameSnapshot())
                .imageUrlSnapshot(item.getImageUrlSnapshot())
                .build();
    }
}
