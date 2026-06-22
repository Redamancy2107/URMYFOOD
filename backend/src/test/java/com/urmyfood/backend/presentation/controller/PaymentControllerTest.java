package com.urmyfood.backend.presentation.controller;

import com.urmyfood.backend.application.dto.ApiResponse;
import com.urmyfood.backend.application.service.OrderService;
import com.urmyfood.backend.domain.model.Account;
import com.urmyfood.backend.domain.model.Order;
import com.urmyfood.backend.domain.model.OrderStatus;
import com.urmyfood.backend.domain.model.PaymentMethod;
import com.urmyfood.backend.domain.model.PaymentStatus;
import com.urmyfood.backend.infrastructure.payment.PayOsService;
import com.urmyfood.backend.infrastructure.security.CustomAccountDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PayOsService payOsService;

    @Mock
    private OrderService orderService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PaymentController paymentController;

    @Test
    void createPayOsPaymentStoresPaymentPayloadOnFirstCreate() {
        Account customer = account(1L, "Customer");
        Account shop = account(2L, "Shop");
        UUID orderId = UUID.randomUUID();
        Order order = vietQrOrder(orderId, customer, shop);

        when(authentication.getPrincipal()).thenReturn(new CustomAccountDetails(customer));
        when(orderService.findOrderByIdAndCustomer(orderId, customer.getId())).thenReturn(order);
        when(orderService.existsPayosOrderCode(anyLong())).thenReturn(false);
        when(payOsService.createPaymentLink(any(CreatePaymentLinkRequest.class))).thenAnswer(invocation -> {
            CreatePaymentLinkRequest paymentData = invocation.getArgument(0);
            return CreatePaymentLinkResponse.builder()
                    .bin("970422")
                    .accountNumber("123456789")
                    .accountName("URMYFOOD")
                    .amount(paymentData.getAmount())
                    .description(paymentData.getDescription())
                    .orderCode(paymentData.getOrderCode())
                    .currency("VND")
                    .paymentLinkId("payment-link-id")
                    .status(vn.payos.model.v2.paymentRequests.PaymentLinkStatus.PENDING)
                    .checkoutUrl("https://pay.payos.vn/web/order")
                    .qrCode("vietqr-payload")
                    .build();
        });

        ResponseEntity<ApiResponse<CreatePaymentLinkResponse>> response =
                paymentController.createPayOsPayment(orderId, authentication);

        CreatePaymentLinkResponse data = response.getBody().getData();
        assertThat(data.getCheckoutUrl()).isEqualTo("https://pay.payos.vn/web/order");
        assertThat(data.getQrCode()).isEqualTo("vietqr-payload");
        assertThat(data.getOrderCode()).isNotNull();
        verify(orderService).savePayosPaymentData(
                orderId,
                data.getOrderCode(),
                data.getCheckoutUrl(),
                data.getQrCode()
        );
    }

    @Test
    void createPayOsPaymentReturnsCachedPayloadWithoutCallingPayOsAgain() {
        Account customer = account(1L, "Customer");
        Account shop = account(2L, "Shop");
        UUID orderId = UUID.randomUUID();
        Order order = vietQrOrder(orderId, customer, shop);
        order.setPayosOrderCode(123456789L);
        order.setPayosCheckoutUrl("https://pay.payos.vn/web/cached");
        order.setPayosQrCode("cached-vietqr-payload");

        when(authentication.getPrincipal()).thenReturn(new CustomAccountDetails(customer));
        when(orderService.findOrderByIdAndCustomer(orderId, customer.getId())).thenReturn(order);

        ResponseEntity<ApiResponse<CreatePaymentLinkResponse>> response =
                paymentController.createPayOsPayment(orderId, authentication);

        CreatePaymentLinkResponse data = response.getBody().getData();
        assertThat(data.getOrderCode()).isEqualTo(123456789L);
        assertThat(data.getCheckoutUrl()).isEqualTo("https://pay.payos.vn/web/cached");
        assertThat(data.getQrCode()).isEqualTo("cached-vietqr-payload");
        verifyNoInteractions(payOsService);
        verify(orderService, never()).savePayosPaymentData(any(UUID.class), anyLong(), any(), any());
        verify(orderService, never()).existsPayosOrderCode(anyLong());
    }

    @Test
    void createPayOsPaymentRejectsPaidVietQrOrder() {
        Account customer = account(1L, "Customer");
        Account shop = account(2L, "Shop");
        UUID orderId = UUID.randomUUID();
        Order order = vietQrOrder(orderId, customer, shop);
        order.setPaymentStatus(PaymentStatus.PAID);

        when(authentication.getPrincipal()).thenReturn(new CustomAccountDetails(customer));
        when(orderService.findOrderByIdAndCustomer(orderId, customer.getId())).thenReturn(order);

        assertThatThrownBy(() -> paymentController.createPayOsPayment(orderId, authentication))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("thanh toán");

        verifyNoInteractions(payOsService);
        verify(orderService, never()).savePayosPaymentData(any(UUID.class), anyLong(), any(), any());
    }

    @Test
    void createPayOsPaymentRejectsTerminalOrder() {
        Account customer = account(1L, "Customer");
        Account shop = account(2L, "Shop");
        UUID orderId = UUID.randomUUID();
        Order order = vietQrOrder(orderId, customer, shop);
        order.setOrderStatus(OrderStatus.CANCELLED);

        when(authentication.getPrincipal()).thenReturn(new CustomAccountDetails(customer));
        when(orderService.findOrderByIdAndCustomer(orderId, customer.getId())).thenReturn(order);

        assertThatThrownBy(() -> paymentController.createPayOsPayment(orderId, authentication))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("kết thúc");

        verifyNoInteractions(payOsService);
        verify(orderService, never()).savePayosPaymentData(any(UUID.class), anyLong(), any(), any());
    }

    private Account account(Long id, String fullName) {
        return Account.builder()
                .id(id)
                .fullName(fullName)
                .email(fullName.toLowerCase() + "@test.local")
                .role("CUSTOMER")
                .build();
    }

    private Order vietQrOrder(UUID orderId, Account customer, Account shop) {
        return Order.builder()
                .orderId(orderId)
                .customer(customer)
                .shop(shop)
                .totalAmount(BigDecimal.valueOf(50_000))
                .discountAmount(BigDecimal.ZERO)
                .finalAmount(BigDecimal.valueOf(50_000))
                .orderStatus(OrderStatus.PENDING)
                .paymentMethod(PaymentMethod.VIETQR)
                .paymentStatus(PaymentStatus.UNPAID)
                .deliveryAddress("123 Test")
                .build();
    }
}
