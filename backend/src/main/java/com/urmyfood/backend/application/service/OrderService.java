package com.urmyfood.backend.application.service;

import com.urmyfood.backend.application.dto.CancelOrderRequest;
import com.urmyfood.backend.application.dto.CheckoutRequest;
import com.urmyfood.backend.application.dto.CreateOrderReviewRequest;
import com.urmyfood.backend.application.dto.DirectCheckoutRequest;
import com.urmyfood.backend.application.dto.OrderItemResponse;
import com.urmyfood.backend.application.dto.OrderReviewResponse;
import com.urmyfood.backend.application.dto.OrderResponse;
import com.urmyfood.backend.application.dto.ReorderResponse;
import com.urmyfood.backend.application.dto.UpdateOrderStatusRequest;
import com.urmyfood.backend.domain.model.Account;
import com.urmyfood.backend.domain.model.CartItem;
import com.urmyfood.backend.domain.model.Order;
import com.urmyfood.backend.domain.model.OrderItem;
import com.urmyfood.backend.domain.model.OrderReview;
import com.urmyfood.backend.domain.model.OrderStatus;
import com.urmyfood.backend.domain.model.PaymentMethod;
import com.urmyfood.backend.domain.model.PaymentStatus;
import com.urmyfood.backend.domain.model.Post;
import com.urmyfood.backend.domain.model.PostStatus;
import com.urmyfood.backend.domain.model.Voucher;
import com.urmyfood.backend.domain.repository.AccountRepository;
import com.urmyfood.backend.domain.repository.CartItemRepository;
import com.urmyfood.backend.domain.repository.OrderReviewRepository;
import com.urmyfood.backend.domain.repository.OrderRepository;
import com.urmyfood.backend.domain.repository.PostRepository;
import com.urmyfood.backend.domain.repository.VoucherRepository;
import com.urmyfood.backend.domain.repository.ShopProfileRepository;
import com.urmyfood.backend.domain.model.ShopProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderReviewRepository orderReviewRepository;
    private final CartItemRepository cartItemRepository;
    private final PostRepository postRepository;
    private final AccountRepository accountRepository;
    private final VoucherRepository voucherRepository;
    private final ShopProfileRepository shopProfileRepository;

    @org.springframework.beans.factory.annotation.Value("${app.order.payment-timeout-minutes:15}")
    private int paymentExpireMinutes;

    @Transactional
    public OrderResponse checkout(Long customerId, CheckoutRequest request) {
        Account customer = accountRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));
        List<CartItem> cartItems = cartItemRepository.findByCustomerId(customerId);
        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Giỏ hàng đang trống");
        }

        PaymentMethod paymentMethod = parsePaymentMethod(request.getPaymentMethod());
        lockCartPosts(cartItems);
        Account shop = validateCartForCheckout(cartItems);
        BigDecimal totalAmount = calculateTotal(cartItems);
        Voucher voucher = resolveVoucher(request.getVoucherId(), request.getVoucherCode(), totalAmount);
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

    @Transactional
    public OrderResponse directCheckout(Long customerId, DirectCheckoutRequest request) {
        Account customer = accountRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay tai khoan"));

        PaymentMethod paymentMethod = parsePaymentMethod(request.getPaymentMethod());
        Post post = lockPostsById(List.of(request.getPostId())).get(request.getPostId());
        Account shop = validatePostForCheckout(post, request.getQuantity());
        BigDecimal totalAmount = post.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));
        Voucher voucher = resolveVoucher(request.getVoucherId(), request.getVoucherCode(), totalAmount);
        BigDecimal discountAmount = voucher == null ? BigDecimal.ZERO : voucher.getDiscountValue().min(totalAmount);
        BigDecimal finalAmount = totalAmount.subtract(discountAmount).max(BigDecimal.ZERO);

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
                .items(List.of(toOrderItemSnapshot(post, request.getQuantity())))
                .build();

        decreasePostQuantity(post, request.getQuantity());
        return toResponse(orderRepository.save(order));
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

    public Order findOrderByIdAndCustomer(UUID orderId, Long customerId) {
        return findOwnedOrder(customerId, orderId);
    }

    @Transactional
    public OrderResponse cancelOrder(Long customerId, UUID orderId, CancelOrderRequest request) {
        Order order = findOwnedOrderForUpdate(customerId, orderId);
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new IllegalArgumentException("Không thể hủy đơn hàng đã thanh toán. Vui lòng liên hệ Tổng đài hỗ trợ.");
        }

        if (order.getOrderStatus() == OrderStatus.PENDING) {
            if (order.getCreatedAt() != null && order.getCreatedAt().plusMinutes(5).isBefore(OffsetDateTime.now())) {
                throw new IllegalArgumentException("Đã quá thời hạn 5 phút để hủy đơn hàng chờ xác nhận");
            }
        } else if (order.getOrderStatus() == OrderStatus.ACCEPTED) {
            if (order.getPaymentMethod() != PaymentMethod.VIETQR) {
                throw new IllegalArgumentException("Chỉ đơn hàng VietQR chưa thanh toán mới có thể hủy sau khi quán đã xác nhận");
            }
            if (order.getUpdatedAt() != null && order.getUpdatedAt().plusMinutes(paymentExpireMinutes).isBefore(OffsetDateTime.now())) {
                throw new IllegalArgumentException("Đã quá thời gian đếm ngược để hủy đơn hàng VietQR");
            }
        } else {
            throw new IllegalArgumentException("Không thể hủy đơn hàng ở trạng thái hiện tại");
        }

        restorePostQuantities(order.getItems());
        order.setOrderStatus(OrderStatus.CANCELLED);
        order.setCancelReason(request.getCancelReason());
        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public void cancelOrderSystem(UUID orderId, String reason) {
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng"));
        
        if (order.getOrderStatus() == OrderStatus.CANCELLED || order.getOrderStatus() == OrderStatus.COMPLETED) {
            return;
        }

        restorePostQuantities(order.getItems());
        order.setOrderStatus(OrderStatus.CANCELLED);
        order.setCancelReason(reason);
        orderRepository.save(order);
    }

    public OrderReviewResponse getReview(Long customerId, UUID orderId) {
        findOwnedOrder(customerId, orderId);
        OrderReview review = orderReviewRepository.findByOrderIdAndCustomerId(orderId, customerId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));
        return toReviewResponse(review);
    }

    @Transactional
    public OrderReviewResponse createReview(Long customerId, UUID orderId, CreateOrderReviewRequest request) {
        Order order = findOwnedOrder(customerId, orderId);
        if (order.getOrderStatus() != OrderStatus.COMPLETED) {
            throw new IllegalArgumentException("Only completed orders can be reviewed");
        }
        if (orderReviewRepository.existsByOrderId(orderId)) {
            throw new IllegalArgumentException("Order was already reviewed");
        }
        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        String comment = request.getComment() == null || request.getComment().isBlank()
                ? null
                : request.getComment().trim();
        OrderReview saved = orderReviewRepository.save(OrderReview.builder()
                .orderId(order.getOrderId())
                .customerId(customerId)
                .shopId(order.getShop().getId())
                .rating(request.getRating())
                .comment(comment)
                .build());
        return toReviewResponse(saved);
    }

    @Transactional
    public ReorderResponse reorder(Long customerId, UUID orderId) {
        Order order = findOwnedOrder(customerId, orderId);
        Account customer = accountRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        List<CartItem> currentItems = cartItemRepository.findByCustomerId(customerId);
        boolean hasOtherShop = currentItems.stream()
                .anyMatch(item -> !item.getPost().getAuthor().getId().equals(order.getShop().getId()));
        if (hasOtherShop) {
            throw new IllegalArgumentException("Cart contains items from another shop");
        }

        Map<UUID, Post> lockedPosts = lockPostsById(order.getItems().stream()
                .map(item -> item.getPost().getPostId())
                .toList());
        List<ReorderResponse.SkippedItem> skippedItems = new ArrayList<>();
        int addedCount = 0;

        for (OrderItem item : order.getItems()) {
            Post post = lockedPosts.get(item.getPost().getPostId());
            String skipReason = reorderSkipReason(post, item.getQuantity());
            if (skipReason != null) {
                skippedItems.add(toSkippedItem(item, skipReason));
                continue;
            }

            CartItem cartItem = cartItemRepository.findByCustomerIdAndPostId(customerId, post.getPostId())
                    .orElse(CartItem.builder()
                            .customer(customer)
                            .post(post)
                            .quantity(0)
                            .build());
            int newQuantity = cartItem.getQuantity() + item.getQuantity();
            if (newQuantity > post.getRemainingQuantity()) {
                skippedItems.add(toSkippedItem(item, "not_enough_quantity"));
                continue;
            }
            cartItem.setQuantity(newQuantity);
            cartItemRepository.save(cartItem);
            addedCount++;
        }

        if (addedCount == 0) {
            throw new IllegalArgumentException("No item can be reordered");
        }

        return ReorderResponse.builder()
                .addedCount(addedCount)
                .skippedItems(skippedItems)
                .build();
    }

    public List<OrderResponse> getShopOrders(Long shopId) {
        return orderRepository.findByShopId(shopId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public OrderResponse getShopOrderDetail(Long shopId, UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng"));
        if (!order.getShop().getId().equals(shopId)) {
            throw new IllegalArgumentException("Đơn hàng không thuộc quán của bạn");
        }
        return toResponse(order);
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long shopId, UUID orderId, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng"));
        if (!order.getShop().getId().equals(shopId)) {
            throw new IllegalArgumentException("Đơn hàng không thuộc quán của bạn");
        }

        OrderStatus newStatus = parseOrderStatus(request.getStatus());
        validateStatusTransition(order, newStatus, request.getRejectReason());

        if (newStatus == OrderStatus.REJECTED) {
            restorePostQuantities(order.getItems());
            order.setCancelReason(request.getRejectReason());
        }
        if (newStatus == OrderStatus.COMPLETED && order.getPaymentMethod() == PaymentMethod.COD) {
            order.setPaymentStatus(PaymentStatus.PAID);
        }

        order.setOrderStatus(newStatus);
        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public void savePayosOrderCode(UUID orderId, Long payosOrderCode) {
        savePayosPaymentData(orderId, payosOrderCode, null, null);
    }

    @Transactional
    public void savePayosPaymentData(UUID orderId, Long payosOrderCode, String checkoutUrl, String qrCode) {
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng"));
        order.setPayosOrderCode(payosOrderCode);
        order.setPayosCheckoutUrl(checkoutUrl);
        order.setPayosQrCode(qrCode);
        orderRepository.save(order);
    }

    @Transactional
    public void markOrderAsPaidByOrderCode(Long payosOrderCode) {
        Order order = orderRepository.findByPayosOrderCode(payosOrderCode)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng với mã PayOS: " + payosOrderCode));
        order.setPaymentStatus(PaymentStatus.PAID);
        orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public boolean existsPayosOrderCode(Long payosOrderCode) {
        return orderRepository.findByPayosOrderCode(payosOrderCode).isPresent();
    }

    private OrderStatus parseOrderStatus(String value) {
        try {
            return OrderStatus.valueOf(value.trim().toUpperCase());
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Trạng thái đơn hàng không hợp lệ");
        }
    }

    private void validateStatusTransition(Order order, OrderStatus next, String rejectReason) {
        OrderStatus current = order.getOrderStatus();
        boolean valid = switch (current) {
            case PENDING -> next == OrderStatus.ACCEPTED || next == OrderStatus.REJECTED;
            case ACCEPTED -> next == OrderStatus.PICKING_UP;
            case PICKING_UP -> next == OrderStatus.DELIVERING;
            case DELIVERING -> next == OrderStatus.COMPLETED;
            default -> false;
        };
        if (!valid) {
            throw new IllegalArgumentException(
                    "Không thể chuyển trạng thái từ " + current + " sang " + next);
        }
        if (next == OrderStatus.REJECTED && order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new IllegalArgumentException("Không thể từ chối đơn hàng đã thanh toán. Vui lòng liên hệ Tổng đài hỗ trợ.");
        }
        if (next == OrderStatus.REJECTED && (rejectReason == null || rejectReason.isBlank())) {
            throw new IllegalArgumentException("Phải nhập lý do khi từ chối đơn hàng");
        }
    }

    private Order findOwnedOrderForUpdate(Long customerId, UUID orderId) {
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng"));
        if (!order.getCustomer().getId().equals(customerId)) {
            throw new IllegalArgumentException("Bạn không có quyền xem đơn hàng này");
        }
        return order;
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
        
        ShopProfile profile = shopProfileRepository.findByShopId(shop.getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin cửa hàng"));
        if (!Boolean.TRUE.equals(profile.getIsOpen()) || !isWithinOpeningHours(profile.getOpeningHours())) {
            throw new IllegalArgumentException("Shop hiện đang không hoạt động");
        }

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

    private Account validatePostForCheckout(Post post, int quantity) {
        Account shop = post.getAuthor();
        ShopProfile profile = shopProfileRepository.findByShopId(shop.getId())
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay thong tin cua hang"));
        if (!Boolean.TRUE.equals(profile.getIsOpen()) || !isWithinOpeningHours(profile.getOpeningHours())) {
            throw new IllegalArgumentException("Shop hien dang khong hoat dong");
        }
        if (post.getStatus() != PostStatus.ACTIVE) {
            throw new IllegalArgumentException("Mon " + post.getDishName() + " hien khong the dat");
        }
        if (quantity > post.getRemainingQuantity()) {
            throw new IllegalArgumentException("Mon " + post.getDishName() + " khong con du so luong");
        }
        return shop;
    }

    private boolean isWithinOpeningHours(String openingHours) {
        if (openingHours == null || openingHours.trim().isEmpty()) {
            return true;
        }
        try {
            String[] parts = openingHours.split("-");
            if (parts.length != 2) {
                return true;
            }
            String startStr = parts[0].trim();
            String endStr = parts[1].trim();

            java.time.ZonedDateTime nowVietnam = java.time.ZonedDateTime.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));
            java.time.LocalTime nowTime = nowVietnam.toLocalTime();

            java.time.LocalTime startTime = java.time.LocalTime.parse(startStr);
            java.time.LocalTime endTime = java.time.LocalTime.parse(endStr);

            if (startTime.isBefore(endTime)) {
                return !nowTime.isBefore(startTime) && !nowTime.isAfter(endTime);
            } else {
                return !nowTime.isBefore(startTime) || !nowTime.isAfter(endTime);
            }
        } catch (Exception e) {
            return true;
        }
    }

    private Voucher resolveVoucher(Long voucherId, String voucherCode, BigDecimal totalAmount) {
        if (voucherId == null && (voucherCode == null || voucherCode.trim().isEmpty())) {
            return null;
        }
        Voucher voucher = null;
        if (voucherId != null) {
            voucher = voucherRepository.findById(voucherId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy voucher"));
        } else {
            voucher = voucherRepository.findByCode(voucherCode.trim())
                    .orElseThrow(() -> new IllegalArgumentException("Mã voucher không tồn tại hoặc đã hết hạn"));
        }
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

    private void lockCartPosts(List<CartItem> cartItems) {
        Map<UUID, Post> lockedPosts = lockPostsById(cartItems.stream()
                .map(item -> item.getPost().getPostId())
                .toList());
        cartItems.forEach(item -> item.setPost(lockedPosts.get(item.getPost().getPostId())));
    }

    private Map<UUID, Post> lockPostsById(List<UUID> postIds) {
        return postIds.stream()
                .distinct()
                .sorted(Comparator.comparing(UUID::toString))
                .map(postId -> postRepository.findByIdForUpdate(postId)
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy món ăn")))
                .collect(Collectors.toMap(Post::getPostId, Function.identity()));
    }

    private void decreasePostQuantities(List<CartItem> cartItems) {
        cartItems.forEach(item -> {
            Post post = item.getPost();
            decreasePostQuantity(post, item.getQuantity());
        });
    }

    private void decreasePostQuantity(Post post, int quantity) {
        int remainingQuantity = post.getRemainingQuantity() - quantity;
        post.setRemainingQuantity(remainingQuantity);
        if (remainingQuantity == 0) {
            post.setStatus(PostStatus.SOLD_OUT);
        }
        postRepository.save(post);
    }

    private void restorePostQuantities(List<OrderItem> orderItems) {
        Map<UUID, Post> lockedPosts = lockPostsById(orderItems.stream()
                .map(item -> item.getPost().getPostId())
                .toList());
        orderItems.forEach(item -> {
            Post post = lockedPosts.get(item.getPost().getPostId());
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
                .customerName(order.getCustomer().getFullName())
                .customerPhone(order.getCustomer().getPhone())
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
                .reviewed(orderReviewRepository.existsByOrderId(order.getOrderId()))
                .items(itemResponses)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
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

    private String reorderSkipReason(Post post, int requestedQuantity) {
        if (post.getStatus() != PostStatus.ACTIVE) {
            return "inactive";
        }
        if (post.getRemainingQuantity() <= 0) {
            return "out_of_stock";
        }
        if (requestedQuantity > post.getRemainingQuantity()) {
            return "not_enough_quantity";
        }
        return null;
    }

    private ReorderResponse.SkippedItem toSkippedItem(OrderItem item, String reason) {
        return ReorderResponse.SkippedItem.builder()
                .postId(item.getPost().getPostId())
                .dishName(item.getDishNameSnapshot())
                .reason(reason)
                .build();
    }

    private OrderReviewResponse toReviewResponse(OrderReview review) {
        return OrderReviewResponse.builder()
                .id(review.getId())
                .orderId(review.getOrderId())
                .customerId(review.getCustomerId())
                .shopId(review.getShopId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
