package com.urmyfood.backend.application.service;

import com.urmyfood.backend.application.dto.CancelOrderRequest;
import com.urmyfood.backend.application.dto.CheckoutRequest;
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
import com.urmyfood.backend.domain.repository.OrderRepository;
import com.urmyfood.backend.domain.repository.PostRepository;
import com.urmyfood.backend.domain.repository.VoucherRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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
    private OrderRepository orderRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private VoucherRepository voucherRepository;

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

        orderService.checkout(customer.getId(), checkoutRequest());

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());
        assertThat(postCaptor.getValue().getRemainingQuantity()).isZero();
        assertThat(postCaptor.getValue().getStatus()).isEqualTo(PostStatus.SOLD_OUT);
    }

    @Test
    void cancelOrderLocksOrderAndPostBeforeRestoringStock() {
        Account customer = account(1L, "Customer");
        Account shop = account(2L, "Shop");
        UUID postId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        Post orderPost = post(postId, "Bánh mì", shop, 0, PostStatus.SOLD_OUT);
        Post lockedPost = post(postId, "Bánh mì", shop, 0, PostStatus.SOLD_OUT);
        Order order = Order.builder()
                .orderId(orderId)
                .customer(customer)
                .shop(shop)
                .orderStatus(OrderStatus.PENDING)
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
                .hasMessage("Chỉ có thể hủy đơn hàng đang chờ xác nhận");

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
        Order order = Order.builder()
                .orderId(orderId)
                .customer(owner)
                .shop(shop)
                .orderStatus(OrderStatus.PENDING)
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

        assertThatThrownBy(() -> orderService.cancelOrder(otherCustomer.getId(), orderId, new CancelOrderRequest("Đổi ý")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Bạn không có quyền xem đơn hàng này");

        verify(orderRepository).findByIdForUpdate(orderId);
        verify(orderRepository, never()).findById(orderId);
        verify(postRepository, never()).findByIdForUpdate(postId);
        verify(postRepository, never()).save(any(Post.class));
        verify(orderRepository, never()).save(any(Order.class));
    }

    private CheckoutRequest checkoutRequest() {
        CheckoutRequest request = new CheckoutRequest();
        request.setPaymentMethod("COD");
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
}
