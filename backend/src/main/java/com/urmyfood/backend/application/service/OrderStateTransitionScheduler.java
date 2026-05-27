package com.urmyfood.backend.application.service;

import com.urmyfood.backend.domain.model.OrderStatus;
import com.urmyfood.backend.domain.model.PaymentStatus;
import com.urmyfood.backend.infrastructure.persistence.entity.OrderEntity;
import com.urmyfood.backend.infrastructure.persistence.repository.JpaOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderStateTransitionScheduler {

    private final JpaOrderRepository jpaOrderRepository;

    @Scheduled(fixedDelay = 15000)
    @Transactional
    public void transitionOrderStates() {
        List<OrderEntity> activeOrders = jpaOrderRepository.findAll().stream()
                .filter(order -> isMutableStatus(order.getOrderStatus()))
                .toList();

        if (activeOrders.isEmpty()) {
            return;
        }

        log.info("Quét thấy {} đơn hàng đang hoạt động, tiến hành mô phỏng cập nhật trạng thái...", activeOrders.size());

        for (OrderEntity order : activeOrders) {
            OrderStatus current = order.getOrderStatus();
            OrderStatus next = getNextStatus(current);
            if (next != null) {
                order.setOrderStatus(next);
                if (next == OrderStatus.COMPLETED) {
                    order.setPaymentStatus(PaymentStatus.PAID);
                }
                jpaOrderRepository.save(order);
                log.info("Đơn hàng {} chuyển từ {} -> {}", order.getOrderId(), current, next);
            }
        }
    }

    private boolean isMutableStatus(OrderStatus status) {
        return status == OrderStatus.PENDING ||
               status == OrderStatus.ACCEPTED ||
               status == OrderStatus.PICKING_UP ||
               status == OrderStatus.DELIVERING;
    }

    private OrderStatus getNextStatus(OrderStatus current) {
        return switch (current) {
            case PENDING -> OrderStatus.ACCEPTED;
            case ACCEPTED -> OrderStatus.PICKING_UP;
            case PICKING_UP -> OrderStatus.DELIVERING;
            case DELIVERING -> OrderStatus.COMPLETED;
            default -> null;
        };
    }
}
