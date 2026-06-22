package com.urmyfood.backend.application.service;

import com.urmyfood.backend.application.dto.CancelOrderRequest;
import com.urmyfood.backend.application.dto.CheckoutRequest;
import com.urmyfood.backend.application.dto.DirectCheckoutRequest;
import com.urmyfood.backend.application.dto.UpdateOrderStatusRequest;
import com.urmyfood.backend.domain.model.Account;
import com.urmyfood.backend.domain.model.CartItem;
import com.urmyfood.backend.domain.model.Order;
import com.urmyfood.backend.domain.model.OrderItem;
import com.urmyfood.backend.domain.model.OrderStatus;
import com.urmyfood.backend.domain.model.PaymentMethod;
import com.urmyfood.backend.domain.model.PaymentStatus;
import com.urmyfood.backend.domain.model.Post;
import com.urmyfood.backend.domain.model.PostStatus;
import com.urmyfood.backend.domain.repository.AccountRepository;
import com.urmyfood.backend.domain.repository.CartItemRepository;
import com.urmyfood.backend.domain.repository.OrderReviewRepository;
import com.urmyfood.backend.domain.repository.OrderRepository;
import com.urmyfood.backend.domain.repository.PostRepository;
import com.urmyfood.backend.domain.repository.VoucherRepository;
import com.urmyfood.backend.domain.model.ShopProfile;
import com.urmyfood.backend.domain.repository.ShopProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderReviewRepository orderReviewRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private VoucherRepository voucherRepository;

    @Mock
    private ShopProfileRepository shopProfileRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void checkoutLocksPostsInStableOrderBeforeDecreasingStock() {
        Account customer = account(1L, "Customer");
        Account shop = account(2L, "Shop");
        UUID laterPostId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
        UUID earlierPostId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        Post laterCartPost = post(laterPostId, "Món B", shop, 5, PostStatus.ACTIVE);
        Post earlierCartPost = post(earlierPostId, "Món A", shop, 5, PostStatus.ACTIVE);
        Post laterLockedPost = post(laterPostId, "Món B", shop, 5, PostStatus.ACTIVE);
        Post earlierLockedPost = post(earlierPostId, "Món A", shop, 5, PostStatus.ACTIVE);

        when(accountRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(cartItemRepository.findByCustomerId(customer.getId())).thenReturn(List.of(
                cartItem(customer, laterCartPost, 1),
                cartItem(customer, earlierCartPost, 1)
        ));
        when(postRepository.findByIdForUpdate(earlierPostId)).thenReturn(Optional.of(earlierLockedPost));
        when(postRepository.findByIdForUpdate(laterPostId)).thenReturn(Optional.of(laterLockedPost));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(shopProfileRepository.findByShopId(shop.getId())).thenReturn(Optional.of(shopProfile()));

        orderService.checkout(customer.getId(), checkoutRequest());

        InOrder inOrder = inOrder(postRepository);
        inOrder.verify(postRepository).findByIdForUpdate(earlierPostId);
        inOrder.verify(postRepository).findByIdForUpdate(laterPostId);
        assertThat(earlierLockedPost.getRemainingQuantity()).isEqualTo(4);
        assertThat(laterLockedPost.getRemainingQuantity()).isEqualTo(4);
        verify(cartItemRepository).deleteByCustomerId(customer.getId());
    }

    @Test
    void checkoutFailsWhenLockedPostDoesNotHaveEnoughStock() {
        Account customer = account(1L, "Customer");
        Account shop = account(2L, "Shop");
        UUID postId = UUID.randomUUID();
        Post cartPost = post(postId, "Cơm gà", shop, 5, PostStatus.ACTIVE);
        Post lockedPost = post(postId, "Cơm gà", shop, 1, PostStatus.ACTIVE);

        when(accountRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(cartItemRepository.findByCustomerId(customer.getId()))
                .thenReturn(List.of(cartItem(customer, cartPost, 2)));
        when(postRepository.findByIdForUpdate(postId)).thenReturn(Optional.of(lockedPost));
        when(shopProfileRepository.findByShopId(shop.getId())).thenReturn(Optional.of(shopProfile()));

        assertThatThrownBy(() -> orderService.checkout(customer.getId(), checkoutRequest()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Món Cơm gà không còn đủ số lượng");

        assertThat(lockedPost.getRemainingQuantity()).isEqualTo(1);
        verify(orderRepository, never()).save(any(Order.class));
        verify(cartItemRepository, never()).deleteByCustomerId(customer.getId());
    }

    @Test
    void checkoutMarksPostSoldOutWhenStockReachesZero() {
        Account customer = account(1L, "Customer");
        Account shop = account(2L, "Shop");
        UUID postId = UUID.randomUUID();
        Post cartPost = post(postId, "Bún bò", shop, 2, PostStatus.ACTIVE);
        Post lockedPost = post(postId, "Bún bò", shop, 2, PostStatus.ACTIVE);

        when(accountRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(cartItemRepository.findByCustomerId(customer.getId()))
                .thenReturn(List.of(cartItem(customer, cartPost, 2)));
        when(postRepository.findByIdForUpdate(postId)).thenReturn(Optional.of(lockedPost));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(shopProfileRepository.findByShopId(shop.getId())).thenReturn(Optional.of(shopProfile()));

        orderService.checkout(customer.getId(), checkoutRequest());

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());
        assertThat(postCaptor.getValue().getRemainingQuantity()).isZero();
        assertThat(postCaptor.getValue().getStatus()).isEqualTo(PostStatus.SOLD_OUT);
    }

    @Test
    void checkoutAcceptsVietqrPaymentMethod() {
        Account customer = account(1L, "Customer");
        Account shop = account(2L, "Shop");
        UUID postId = UUID.randomUUID();
        Post cartPost = post(postId, "Cơm gà", shop, 3, PostStatus.ACTIVE);
        Post lockedPost = post(postId, "Cơm gà", shop, 3, PostStatus.ACTIVE);

        when(accountRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(cartItemRepository.findByCustomerId(customer.getId()))
                .thenReturn(List.of(cartItem(customer, cartPost, 1)));
        when(postRepository.findByIdForUpdate(postId)).thenReturn(Optional.of(lockedPost));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(shopProfileRepository.findByShopId(shop.getId())).thenReturn(Optional.of(shopProfile()));

        orderService.checkout(customer.getId(), checkoutRequest("VIETQR"));

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getPaymentMethod()).isEqualTo(PaymentMethod.VIETQR);
        assertThat(orderCaptor.getValue().getPaymentStatus()).isEqualTo(PaymentStatus.UNPAID);
    }

    @Test
    void directCheckoutCreatesOneItemOrderAndDoesNotDeleteCart() {
        Account customer = account(1L, "Customer");
        Account shop = account(2L, "Shop");
        UUID postId = UUID.randomUUID();
        Post lockedPost = post(postId, "Com ga", shop, 5, PostStatus.ACTIVE);

        when(accountRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(postRepository.findByIdForUpdate(postId)).thenReturn(Optional.of(lockedPost));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(shopProfileRepository.findByShopId(shop.getId())).thenReturn(Optional.of(shopProfile()));

        orderService.directCheckout(customer.getId(), directCheckoutRequest(postId, 2));

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();

        assertThat(savedOrder.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(savedOrder.getPaymentMethod()).isEqualTo(PaymentMethod.COD);
        assertThat(savedOrder.getPaymentStatus()).isEqualTo(PaymentStatus.UNPAID);
        assertThat(savedOrder.getItems()).hasSize(1);
        assertThat(savedOrder.getItems().get(0).getPost().getPostId()).isEqualTo(postId);
        assertThat(savedOrder.getItems().get(0).getQuantity()).isEqualTo(2);
        assertThat(savedOrder.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(20_000));
        assertThat(lockedPost.getRemainingQuantity()).isEqualTo(3);
        verify(cartItemRepository, never()).deleteByCustomerId(customer.getId());
    }

    @Test
    void directCheckoutFailsWhenLockedPostDoesNotHaveEnoughStock() {
        Account customer = account(1L, "Customer");
        Account shop = account(2L, "Shop");
        UUID postId = UUID.randomUUID();
        Post lockedPost = post(postId, "Com ga", shop, 1, PostStatus.ACTIVE);

        when(accountRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(postRepository.findByIdForUpdate(postId)).thenReturn(Optional.of(lockedPost));
        when(shopProfileRepository.findByShopId(shop.getId())).thenReturn(Optional.of(shopProfile()));

        assertThatThrownBy(() -> orderService.directCheckout(customer.getId(), directCheckoutRequest(postId, 2)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Mon Com ga khong con du so luong");

        assertThat(lockedPost.getRemainingQuantity()).isEqualTo(1);
        verify(orderRepository, never()).save(any(Order.class));
        verify(cartItemRepository, never()).deleteByCustomerId(customer.getId());
    }

    @Test
    void directCheckoutFailsWhenShopIsClosed() {
        Account customer = account(1L, "Customer");
        Account shop = account(2L, "Shop");
        UUID postId = UUID.randomUUID();
        Post lockedPost = post(postId, "Com ga", shop, 5, PostStatus.ACTIVE);

        when(accountRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(postRepository.findByIdForUpdate(postId)).thenReturn(Optional.of(lockedPost));
        when(shopProfileRepository.findByShopId(shop.getId())).thenReturn(Optional.of(shopProfile(false)));

        assertThatThrownBy(() -> orderService.directCheckout(customer.getId(), directCheckoutRequest(postId, 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Shop hien dang khong hoat dong");

        verify(orderRepository, never()).save(any(Order.class));
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void directCheckoutFailsWhenPostIsNotActive() {
        Account customer = account(1L, "Customer");
        Account shop = account(2L, "Shop");
        UUID postId = UUID.randomUUID();
        Post lockedPost = post(postId, "Com ga", shop, 5, PostStatus.INACTIVE);

        when(accountRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(postRepository.findByIdForUpdate(postId)).thenReturn(Optional.of(lockedPost));
        when(shopProfileRepository.findByShopId(shop.getId())).thenReturn(Optional.of(shopProfile()));

        assertThatThrownBy(() -> orderService.directCheckout(customer.getId(), directCheckoutRequest(postId, 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Mon Com ga hien khong the dat");

        verify(orderRepository, never()).save(any(Order.class));
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void directCheckoutRejectsInvalidPaymentMethod() {
        Account customer = account(1L, "Customer");
        UUID postId = UUID.randomUUID();

        when(accountRepository.findById(customer.getId())).thenReturn(Optional.of(customer));

        assertThatThrownBy(() -> orderService.directCheckout(
                customer.getId(),
                directCheckoutRequest(postId, 1, "MO" + "MO")))
                .isInstanceOf(IllegalArgumentException.class);

        verify(postRepository, never()).findByIdForUpdate(any(UUID.class));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void checkoutRejectsLegacyOnlineWalletPaymentMethods() {
        Account customer = account(1L, "Customer");
        Account shop = account(2L, "Shop");
        Post cartPost = post(UUID.randomUUID(), "Cơm gà", shop, 3, PostStatus.ACTIVE);

        when(accountRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(cartItemRepository.findByCustomerId(customer.getId()))
                .thenReturn(List.of(cartItem(customer, cartPost, 1)));

        assertThatThrownBy(() -> orderService.checkout(customer.getId(), checkoutRequest("MO" + "MO")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Phương thức thanh toán không hợp lệ");
        assertThatThrownBy(() -> orderService.checkout(customer.getId(), checkoutRequest("ZA" + "LO" + "PAY")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Phương thức thanh toán không hợp lệ");

        verify(postRepository, never()).findByIdForUpdate(any(UUID.class));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void cancelOrderLocksOrderAndPostBeforeRestoringStock() {
        Account customer = account(1L, "Customer");
        Account shop = account(2L, "Shop");
        UUID postId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        Post orderPost = post(postId, "Bánh mì", shop, 0, PostStatus.SOLD_OUT);
        Post lockedPost = post(postId, "Bánh mì", shop, 0, PostStatus.SOLD_OUT);
        Order order = pendingOrder(orderId, customer, shop, orderPost, 2);

        when(orderRepository.findByIdForUpdate(orderId)).thenReturn(Optional.of(order));
        when(postRepository.findByIdForUpdate(postId)).thenReturn(Optional.of(lockedPost));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        orderService.cancelOrder(customer.getId(), orderId, new CancelOrderRequest("Đổi ý"));

        verify(orderRepository).findByIdForUpdate(orderId);
        verify(orderRepository, never()).findById(orderId);
        assertThat(lockedPost.getRemainingQuantity()).isEqualTo(2);
        assertThat(lockedPost.getStatus()).isEqualTo(PostStatus.ACTIVE);
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(postRepository).save(lockedPost);
    }

    @Test
    void cancelOrderFailsWhenOrderIsNotPendingWithoutRestoringStock() {
        Account customer = account(1L, "Customer");
        Account shop = account(2L, "Shop");
        UUID postId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        Post orderPost = post(postId, "Bánh mì", shop, 0, PostStatus.SOLD_OUT);
        Order order = Order.builder()
                .orderId(orderId)
                .customer(customer)
                .shop(shop)
                .orderStatus(OrderStatus.CANCELLED)
                .paymentMethod(PaymentMethod.COD)
                .paymentStatus(PaymentStatus.UNPAID)
                .totalAmount(BigDecimal.valueOf(20_000))
                .discountAmount(BigDecimal.ZERO)
                .finalAmount(BigDecimal.valueOf(20_000))
                .deliveryAddress("123 Test")
                .items(List.of(OrderItem.builder()
                        .post(orderPost)
                        .quantity(2)
                        .priceAtPurchase(BigDecimal.valueOf(10_000))
                        .dishNameSnapshot("Bánh mì")
                        .build()))
                .build();

        when(orderRepository.findByIdForUpdate(orderId)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(customer.getId(), orderId, new CancelOrderRequest("Đổi ý")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Không thể hủy đơn hàng ở trạng thái hiện tại");

        verify(orderRepository).findByIdForUpdate(orderId);
        verify(orderRepository, never()).findById(orderId);
        verify(postRepository, never()).findByIdForUpdate(postId);
        verify(postRepository, never()).save(any(Post.class));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void cancelOrderFailsWhenCustomerDoesNotOwnLockedOrderWithoutRestoringStock() {
        Account owner = account(1L, "Customer");
        Account otherCustomer = account(3L, "Other Customer");
        Account shop = account(2L, "Shop");
        UUID postId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        Post orderPost = post(postId, "Bánh mì", shop, 0, PostStatus.SOLD_OUT);
        Order order = pendingOrder(orderId, owner, shop, orderPost, 2);

        when(orderRepository.findByIdForUpdate(orderId)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(otherCustomer.getId(), orderId, new CancelOrderRequest("Đổi ý")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Bạn không có quyền xem đơn hàng này");

        verify(orderRepository).findByIdForUpdate(orderId);
        verify(orderRepository, never()).findById(orderId);
        verify(postRepository, never()).findByIdForUpdate(postId);
        verify(postRepository, never()).save(any(Post.class));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void cancelOrderFailsAfter5Minutes() {
        Account customer = account(1L, "Customer");
        Account shop = account(2L, "Shop");
        UUID postId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        Post orderPost = post(postId, "Phở", shop, 0, PostStatus.SOLD_OUT);
        Order order = pendingOrder(orderId, customer, shop, orderPost, 1);
        order.setCreatedAt(OffsetDateTime.now().minusMinutes(6));

        when(orderRepository.findByIdForUpdate(orderId)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(customer.getId(), orderId, new CancelOrderRequest("Đổi ý")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Đã quá thời hạn 5 phút để hủy đơn hàng chờ xác nhận");

        verify(orderRepository, never()).save(any(Order.class));
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void shopAcceptsPendingOrder() {
        Account customer = account(1L, "Customer");
        Account shop = account(2L, "Shop");
        UUID orderId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        Post orderPost = post(postId, "Cơm tấm", shop, 3, PostStatus.ACTIVE);
        Order order = pendingOrder(orderId, customer, shop, orderPost, 1);

        when(orderRepository.findByIdForUpdate(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest("ACCEPTED", null);
        orderService.updateOrderStatus(shop.getId(), orderId, request);

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.ACCEPTED);
    }

    @Test
    void shopCanAcceptUnpaidVietqrOrder() {
        Account customer = account(1L, "Customer");
        Account shop = account(2L, "Shop");
        UUID orderId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        Post orderPost = post(postId, "Cơm tấm", shop, 3, PostStatus.ACTIVE);
        Order order = pendingOrder(orderId, customer, shop, orderPost, 1);
        order.setPaymentMethod(PaymentMethod.VIETQR);
        order.setPaymentStatus(PaymentStatus.UNPAID);

        when(orderRepository.findByIdForUpdate(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest("ACCEPTED", null);
        orderService.updateOrderStatus(shop.getId(), orderId, request);

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.ACCEPTED);
        assertThat(order.getPaymentStatus()).isEqualTo(PaymentStatus.UNPAID);
    }

    @Test
    void shopAcceptsPaidVietqrOrder() {
        Account customer = account(1L, "Customer");
        Account shop = account(2L, "Shop");
        UUID orderId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        Post orderPost = post(postId, "Cơm tấm", shop, 3, PostStatus.ACTIVE);
        Order order = pendingOrder(orderId, customer, shop, orderPost, 1);
        order.setPaymentMethod(PaymentMethod.VIETQR);
        order.setPaymentStatus(PaymentStatus.PAID);

        when(orderRepository.findByIdForUpdate(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest("ACCEPTED", null);
        orderService.updateOrderStatus(shop.getId(), orderId, request);

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.ACCEPTED);
        assertThat(order.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
    }

    @Test
    void shopRejectsOrderWithReasonAndRestoresStock() {
        Account customer = account(1L, "Customer");
        Account shop = account(2L, "Shop");
        UUID orderId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        Post orderPost = post(postId, "Bún chả", shop, 0, PostStatus.SOLD_OUT);
        Post lockedPost = post(postId, "Bún chả", shop, 0, PostStatus.SOLD_OUT);
        Order order = pendingOrder(orderId, customer, shop, orderPost, 2);

        when(orderRepository.findByIdForUpdate(orderId)).thenReturn(Optional.of(order));
        when(postRepository.findByIdForUpdate(postId)).thenReturn(Optional.of(lockedPost));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest("REJECTED", "Hết nguyên liệu");
        orderService.updateOrderStatus(shop.getId(), orderId, request);

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.REJECTED);
        assertThat(order.getCancelReason()).isEqualTo("Hết nguyên liệu");
        assertThat(lockedPost.getRemainingQuantity()).isEqualTo(2);
        assertThat(lockedPost.getStatus()).isEqualTo(PostStatus.ACTIVE);
    }

    @Test
    void shopRejectOrderFailsWithoutReason() {
        Account customer = account(1L, "Customer");
        Account shop = account(2L, "Shop");
        UUID orderId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        Post orderPost = post(postId, "Bánh cuốn", shop, 3, PostStatus.ACTIVE);
        Order order = pendingOrder(orderId, customer, shop, orderPost, 1);

        when(orderRepository.findByIdForUpdate(orderId)).thenReturn(Optional.of(order));

        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest("REJECTED", null);
        assertThatThrownBy(() -> orderService.updateOrderStatus(shop.getId(), orderId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Phải nhập lý do khi từ chối đơn hàng");

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void shopCannotSkipStatusTransition() {
        Account customer = account(1L, "Customer");
        Account shop = account(2L, "Shop");
        UUID orderId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        Post orderPost = post(postId, "Cơm tấm", shop, 3, PostStatus.ACTIVE);
        Order order = pendingOrder(orderId, customer, shop, orderPost, 1);

        when(orderRepository.findByIdForUpdate(orderId)).thenReturn(Optional.of(order));

        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest("DELIVERING", null);
        assertThatThrownBy(() -> orderService.updateOrderStatus(shop.getId(), orderId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Không thể chuyển trạng thái từ PENDING sang DELIVERING");

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void shopCannotUpdateOtherShopOrder() {
        Account customer = account(1L, "Customer");
        Account shop = account(2L, "Shop");
        Account otherShop = account(3L, "Other Shop");
        UUID orderId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        Post orderPost = post(postId, "Cơm tấm", shop, 3, PostStatus.ACTIVE);
        Order order = pendingOrder(orderId, customer, shop, orderPost, 1);

        when(orderRepository.findByIdForUpdate(orderId)).thenReturn(Optional.of(order));

        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest("ACCEPTED", null);
        assertThatThrownBy(() -> orderService.updateOrderStatus(otherShop.getId(), orderId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Đơn hàng không thuộc quán của bạn");

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void shopCompletesDeliveringOrderAndMarksCodAsPaid() {
        Account customer = account(1L, "Customer");
        Account shop = account(2L, "Shop");
        UUID orderId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        Post orderPost = post(postId, "Cơm sườn", shop, 3, PostStatus.ACTIVE);
        Order order = Order.builder()
                .orderId(orderId)
                .customer(customer)
                .shop(shop)
                .orderStatus(OrderStatus.DELIVERING)
                .paymentMethod(PaymentMethod.COD)
                .paymentStatus(PaymentStatus.UNPAID)
                .totalAmount(BigDecimal.valueOf(30_000))
                .discountAmount(BigDecimal.ZERO)
                .finalAmount(BigDecimal.valueOf(30_000))
                .deliveryAddress("KTX Khu A")
                .items(List.of(OrderItem.builder()
                        .post(orderPost)
                        .quantity(1)
                        .priceAtPurchase(BigDecimal.valueOf(30_000))
                        .dishNameSnapshot("Cơm sườn")
                        .build()))
                .build();

        when(orderRepository.findByIdForUpdate(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest("COMPLETED", null);
        orderService.updateOrderStatus(shop.getId(), orderId, request);

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(order.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
    }

    private CheckoutRequest checkoutRequest() {
        return checkoutRequest("COD");
    }

    private CheckoutRequest checkoutRequest(String paymentMethod) {
        CheckoutRequest request = new CheckoutRequest();
        request.setPaymentMethod(paymentMethod);
        request.setDeliveryAddress("123 Test");
        return request;
    }

    private DirectCheckoutRequest directCheckoutRequest(UUID postId, int quantity) {
        return directCheckoutRequest(postId, quantity, "COD");
    }

    private DirectCheckoutRequest directCheckoutRequest(UUID postId, int quantity, String paymentMethod) {
        DirectCheckoutRequest request = new DirectCheckoutRequest();
        request.setPostId(postId);
        request.setQuantity(quantity);
        request.setPaymentMethod(paymentMethod);
        request.setDeliveryAddress("123 Test");
        return request;
    }

    private Account account(Long id, String fullName) {
        return Account.builder()
                .id(id)
                .fullName(fullName)
                .build();
    }

    private Post post(UUID postId, String dishName, Account author, int remainingQuantity, PostStatus status) {
        return Post.builder()
                .postId(postId)
                .dishName(dishName)
                .price(BigDecimal.valueOf(10_000))
                .originalPrice(BigDecimal.valueOf(12_000))
                .maxQuantity(10)
                .remainingQuantity(remainingQuantity)
                .status(status)
                .author(author)
                .build();
    }

    private CartItem cartItem(Account customer, Post post, int quantity) {
        return CartItem.builder()
                .customer(customer)
                .post(post)
                .quantity(quantity)
                .build();
    }

    private Order pendingOrder(UUID orderId, Account customer, Account shop, Post orderPost, int quantity) {
        return Order.builder()
                .orderId(orderId)
                .customer(customer)
                .shop(shop)
                .orderStatus(OrderStatus.PENDING)
                .paymentMethod(PaymentMethod.COD)
                .paymentStatus(PaymentStatus.UNPAID)
                .totalAmount(BigDecimal.valueOf(10_000L * quantity))
                .discountAmount(BigDecimal.ZERO)
                .finalAmount(BigDecimal.valueOf(10_000L * quantity))
                .deliveryAddress("123 Test")
                .createdAt(OffsetDateTime.now())
                .items(List.of(OrderItem.builder()
                        .post(orderPost)
                        .quantity(quantity)
                        .priceAtPurchase(BigDecimal.valueOf(10_000))
                        .dishNameSnapshot(orderPost.getDishName())
                        .build()))
                .build();
    }

    private ShopProfile shopProfile() {
        return shopProfile(true);
    }

    private ShopProfile shopProfile(boolean isOpen) {
        return ShopProfile.builder()
                .isOpen(isOpen)
                .openingHours("00:00 - 23:59")
                .build();
    }
}
