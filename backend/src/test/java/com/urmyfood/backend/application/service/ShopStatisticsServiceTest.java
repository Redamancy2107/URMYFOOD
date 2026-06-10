package com.urmyfood.backend.application.service;

import com.urmyfood.backend.domain.model.Account;
import com.urmyfood.backend.domain.model.OrderStatus;
import com.urmyfood.backend.domain.model.PaymentMethod;
import com.urmyfood.backend.domain.model.PaymentStatus;
import com.urmyfood.backend.domain.model.ShopStatisticsPeriod;
import com.urmyfood.backend.domain.repository.AccountRepository;
import com.urmyfood.backend.infrastructure.persistence.entity.AccountEntity;
import com.urmyfood.backend.infrastructure.persistence.entity.OrderEntity;
import com.urmyfood.backend.infrastructure.persistence.repository.JpaOrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShopStatisticsServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private JpaOrderRepository orderRepository;

    @InjectMocks
    private ShopStatisticsService service;

    @Test
    void getMonthStatisticsCalculatesRevenueAndCancellationRate() {
        Long shopId = 2L;
        when(accountRepository.findById(shopId)).thenReturn(Optional.of(Account.builder()
                .id(shopId)
                .role("SHOP")
                .build()));
        when(orderRepository.findShopOrdersForStatistics(eq(shopId), any(), any())).thenReturn(List.of(
                order(shopId, OrderStatus.COMPLETED, "120000", OffsetDateTime.parse("2026-06-02T10:00:00+07:00")),
                order(shopId, OrderStatus.COMPLETED, "80000", OffsetDateTime.parse("2026-06-03T12:00:00+07:00")),
                order(shopId, OrderStatus.CANCELLED, "50000", OffsetDateTime.parse("2026-06-04T12:00:00+07:00")),
                order(shopId, OrderStatus.PENDING, "30000", OffsetDateTime.parse("2026-06-05T12:00:00+07:00"))
        ));

        var response = service.getStatistics(shopId, ShopStatisticsPeriod.MONTH, null, "2026-06", null);

        assertThat(response.getTotalRevenue()).isEqualByComparingTo("200000");
        assertThat(response.getTotalOrders()).isEqualTo(4);
        assertThat(response.getCancelledOrders()).isEqualTo(1);
        assertThat(response.getCancellationRate()).isEqualTo(25.0);
        assertThat(response.getSelectorText()).isEqualTo("Tháng 06/2026");
        assertThat(response.getEntries()).isNotEmpty();
    }

    @Test
    void getAllStatisticsUsesCompletedOrdersOnlyForRevenue() {
        Long shopId = 2L;
        when(accountRepository.findById(shopId)).thenReturn(Optional.of(Account.builder()
                .id(shopId)
                .role("SHOP")
                .build()));
        when(orderRepository.findShopOrdersForAllTimeStatistics(shopId)).thenReturn(List.of(
                order(shopId, OrderStatus.COMPLETED, "100000", OffsetDateTime.parse("2025-06-02T10:00:00+07:00")),
                order(shopId, OrderStatus.REJECTED, "90000", OffsetDateTime.parse("2026-06-03T12:00:00+07:00"))
        ));

        var response = service.getStatistics(shopId, ShopStatisticsPeriod.ALL, null, null, null);

        assertThat(response.getTotalRevenue()).isEqualByComparingTo("100000");
        assertThat(response.getTotalOrders()).isEqualTo(2);
        assertThat(response.getCancelledOrders()).isEqualTo(1);
        assertThat(response.getEntries()).extracting("label").containsExactly("2025", "2026");
    }

    private OrderEntity order(Long shopId, OrderStatus status, String amount, OffsetDateTime createdAt) {
        OrderEntity order = OrderEntity.builder()
                .shop(AccountEntity.builder().id(shopId).build())
                .customer(AccountEntity.builder().id(1L).build())
                .orderStatus(status)
                .paymentMethod(PaymentMethod.COD)
                .paymentStatus(PaymentStatus.UNPAID)
                .totalAmount(new BigDecimal(amount))
                .discountAmount(BigDecimal.ZERO)
                .finalAmount(new BigDecimal(amount))
                .deliveryAddress("KTX Khu A")
                .build();
        order.setCreatedAt(createdAt);
        return order;
    }
}
