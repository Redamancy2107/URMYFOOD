package com.urmyfood.backend.application.service;

import com.urmyfood.backend.domain.model.Order;
import com.urmyfood.backend.domain.model.OrderStatus;
import com.urmyfood.backend.domain.model.Post;
import com.urmyfood.backend.domain.model.PostStatus;
import com.urmyfood.backend.domain.repository.OrderRepository;
import com.urmyfood.backend.domain.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderStateTransitionScheduler {

    private static final int PENDING_EXPIRE_MINUTES = 10;

    private final OrderRepository orderRepository;
    private final PostRepository postRepository;

    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void expirePendingOrders() {
        OffsetDateTime expiredBefore = OffsetDateTime.now().minusMinutes(PENDING_EXPIRE_MINUTES);
        List<Order> expiredOrders = orderRepository.findPendingExpiredOrders(expiredBefore);

        if (expiredOrders.isEmpty()) {
            return;
        }

        log.info("Phát hiện {} đơn hàng PENDING quá {} phút, tiến hành tự động hủy...",
                expiredOrders.size(), PENDING_EXPIRE_MINUTES);

        for (Order order : expiredOrders) {
            order.setOrderStatus(OrderStatus.EXPIRED);
            order.setCancelReason("Quán không xác nhận trong " + PENDING_EXPIRE_MINUTES + " phút");
            restorePostQuantities(order);
            orderRepository.save(order);
            log.info("Đơn hàng {} đã tự động hết hạn", order.getOrderId());
        }
    }

    private void restorePostQuantities(Order order) {
        order.getItems().forEach(item -> {
            postRepository.findByIdForUpdate(item.getPost().getPostId()).ifPresent(post -> {
                post.setRemainingQuantity(post.getRemainingQuantity() + item.getQuantity());
                if (post.getStatus() == PostStatus.SOLD_OUT && post.getRemainingQuantity() > 0) {
                    post.setStatus(PostStatus.ACTIVE);
                }
                postRepository.save(post);
            });
        });
    }
}
