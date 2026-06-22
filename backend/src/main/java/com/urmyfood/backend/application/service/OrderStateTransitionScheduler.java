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

import com.urmyfood.backend.infrastructure.payment.PayOsService;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderStateTransitionScheduler {

    @org.springframework.beans.factory.annotation.Value("${app.order.pending-timeout-minutes:10}")
    private int pendingExpireMinutes;

    @org.springframework.beans.factory.annotation.Value("${app.order.payment-timeout-minutes:15}")
    private int paymentExpireMinutes;

    private final OrderRepository orderRepository;
    private final PostRepository postRepository;
    private final PayOsService payOsService;

    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void expirePendingOrders() {
        OffsetDateTime expiredBefore = OffsetDateTime.now().minusMinutes(pendingExpireMinutes);
        List<Order> expiredOrders = orderRepository.findPendingExpiredOrders(expiredBefore);

        if (!expiredOrders.isEmpty()) {
            log.info("Phát hiện {} đơn hàng PENDING quá {} phút, tiến hành tự động hủy...",
                    expiredOrders.size(), pendingExpireMinutes);

            for (Order order : expiredOrders) {
                order.setOrderStatus(OrderStatus.EXPIRED);
                order.setCancelReason("Quán không xác nhận trong " + pendingExpireMinutes + " phút");
                restorePostQuantities(order);
                orderRepository.save(order);
                log.info("Đơn hàng {} đã tự động hết hạn", order.getOrderId());
            }
        }
    }

    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void expireAcceptedUnpaidOrders() {
        OffsetDateTime expiredBefore = OffsetDateTime.now().minusMinutes(paymentExpireMinutes);
        List<Order> expiredOrders = orderRepository.findAcceptedUnpaidExpiredOrders(expiredBefore);

        if (!expiredOrders.isEmpty()) {
            log.info("Phát hiện {} đơn hàng ACCEPTED (chờ thanh toán VietQR) quá {} phút, tiến hành hủy...",
                    expiredOrders.size(), paymentExpireMinutes);

            for (Order order : expiredOrders) {
                order.setOrderStatus(OrderStatus.CANCELLED);
                order.setCancelReason("Khách hàng không thanh toán trong " + paymentExpireMinutes + " phút");
                restorePostQuantities(order);
                orderRepository.save(order);
                
                // Hủy mã QR trên PayOS
                if (order.getPayosOrderCode() != null) {
                    payOsService.cancelPaymentLink(order.getPayosOrderCode(), "Quá 15 phút thanh toán");
                }
                log.info("Đơn hàng {} đã tự động hủy do quá hạn thanh toán", order.getOrderId());
            }
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
