package com.urmyfood.backend.application.service;

import com.urmyfood.backend.application.dto.PageResponse;
import com.urmyfood.backend.application.dto.ReportResponse;
import com.urmyfood.backend.domain.model.*;
import com.urmyfood.backend.domain.repository.AccountActionLogRepository;
import com.urmyfood.backend.domain.repository.AccountRepository;
import com.urmyfood.backend.domain.repository.OrderRepository;
import com.urmyfood.backend.domain.repository.PostRepository;
import com.urmyfood.backend.domain.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminReportService {

    private final ReportRepository reportRepository;
    private final PostRepository postRepository;
    private final AccountRepository accountRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final AccountActionLogRepository accountActionLogRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public PageResponse<ReportResponse> getReports(int page, int size, String status) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Report> reportPage = reportRepository.findAllByStatus(status, pageable);
        
        List<ReportResponse> content = reportPage.stream()
                .map(r -> ReportResponse.builder()
                        .reportId(r.getReportId())
                        .postId(r.getPost().getPostId())
                        .postContent(r.getPost().getContent())
                        .reporterName(r.getReporter() != null ? r.getReporter().getFullName() : "Unknown")
                        .reason(r.getReason())
                        .status(r.getStatus() != null ? r.getStatus().name() : "UNKNOWN")
                        .createdAt(r.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
                
        return PageResponse.ofAnchored(content, page, size, reportPage.getTotalElements(), null);
    }

    @Transactional
    public void ignoreReport(UUID reportId, Long adminId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy report"));
        report.setStatus(ReportStatus.IGNORED);
        report.setResolvedBy(getAdminAccount(adminId));
        report.setResolvedAt(OffsetDateTime.now());
        reportRepository.save(report);
        
        logAction(adminId, "REPORT", reportId.toString(), "IGNORE_REPORT", "Đã bỏ qua report");
    }

    @Transactional
    public void deletePost(UUID reportId, String warningMessage, Long adminId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy report"));
        Post post = postRepository.findByIdForUpdate(report.getPost().getPostId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài viết"));
        
        post.setStatus(PostStatus.DELETED);
        postRepository.save(post);
        
        report.setStatus(ReportStatus.RESOLVED);
        report.setResolvedBy(getAdminAccount(adminId));
        report.setResolvedAt(OffsetDateTime.now());
        reportRepository.save(report);
        
        // Cancel pending orders related to this post
        List<Order> pendingOrders = orderRepository.findPendingOrAcceptedOrdersByPostId(post.getPostId());
        for (Order order : pendingOrders) {
            orderService.cancelOrderSystem(order.getOrderId(), "Bài viết đã bị xóa do vi phạm quy định");
        }
        
        // Clear Redis cache
        clearPostCache(post.getPostId(), post.getAuthor().getId());
        
        logAction(adminId, "POST", post.getPostId().toString(), "DELETE_POST", warningMessage);
    }

    @Transactional
    public void blockShop(UUID reportId, int blockDays, String reason, Long adminId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy report"));
        Account shop = accountRepository.findById(report.getPost().getAuthor().getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy shop"));
                
        shop.setActive(false);
        accountRepository.save(shop);
        
        report.setStatus(ReportStatus.RESOLVED);
        report.setResolvedBy(getAdminAccount(adminId));
        report.setResolvedAt(OffsetDateTime.now());
        reportRepository.save(report);
        
        // Cancel pending orders of the shop
        List<Order> pendingOrders = orderRepository.findPendingOrAcceptedOrdersByShopId(shop.getId());
        for (Order order : pendingOrders) {
            orderService.cancelOrderSystem(order.getOrderId(), "Cửa hàng đã bị khóa tạm thời");
        }
        
        clearShopCache(shop.getId());
        logAction(adminId, "SHOP", shop.getId().toString(), "BLOCK_SHOP", reason + " (Khóa " + blockDays + " ngày)");
    }

    @Transactional
    public void deleteShop(UUID reportId, Long adminId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy report"));
        Account shop = accountRepository.findById(report.getPost().getAuthor().getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy shop"));
                
        shop.setActive(false);
        shop.setRole("CUSTOMER"); // Demote
        accountRepository.save(shop);
        
        report.setStatus(ReportStatus.RESOLVED);
        report.setResolvedBy(getAdminAccount(adminId));
        report.setResolvedAt(OffsetDateTime.now());
        reportRepository.save(report);
        
        // Cancel pending orders of the shop
        List<Order> pendingOrders = orderRepository.findPendingOrAcceptedOrdersByShopId(shop.getId());
        for (Order order : pendingOrders) {
            orderService.cancelOrderSystem(order.getOrderId(), "Cửa hàng đã bị xóa vĩnh viễn");
        }
        
        clearShopCache(shop.getId());
        logAction(adminId, "SHOP", shop.getId().toString(), "DELETE_SHOP", "Xóa shop vĩnh viễn");
    }

    private Account getAdminAccount(Long adminId) {
        return accountRepository.findById(adminId).orElse(null);
    }

    private void logAction(Long adminId, String targetType, String targetIdStr, String actionType, String reason) {
        AccountActionLog logEntry = AccountActionLog.builder()
                .targetType(targetType)
                .targetIdStr(targetIdStr)
                .actionType(actionType)
                .reason(reason)
                .build();
        accountActionLogRepository.save(logEntry);
    }

    private void clearPostCache(UUID postId, Long shopId) {
        try {
            redisTemplate.delete("post:detail:" + postId);
            Set<String> keys = redisTemplate.keys("post:feed:*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
            redisTemplate.delete("shop:" + shopId + ":posts");
        } catch (Exception e) {
            log.warn("Failed to clear Redis cache for post", e);
        }
    }

    private void clearShopCache(Long shopId) {
        try {
            redisTemplate.delete("shop:detail:" + shopId);
            Set<String> keys = redisTemplate.keys("post:feed:*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
            redisTemplate.delete("shop:" + shopId + ":posts");
        } catch (Exception e) {
            log.warn("Failed to clear Redis cache for shop", e);
        }
    }
}
