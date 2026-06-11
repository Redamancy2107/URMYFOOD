package com.urmyfood.backend.application.service;

import com.urmyfood.backend.application.dto.ShopStatisticsResponse;
import com.urmyfood.backend.domain.model.Account;
import com.urmyfood.backend.domain.model.OrderStatus;
import com.urmyfood.backend.domain.model.ShopStatisticsPeriod;
import com.urmyfood.backend.domain.repository.AccountRepository;
import com.urmyfood.backend.infrastructure.persistence.entity.OrderEntity;
import com.urmyfood.backend.infrastructure.persistence.repository.JpaOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ShopStatisticsService {

    private static final String ROLE_SHOP = "SHOP";
    private static final ZoneId APP_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final List<OrderStatus> CANCELLED_STATUSES = List.of(
            OrderStatus.CANCELLED,
            OrderStatus.REJECTED,
            OrderStatus.EXPIRED
    );

    private final AccountRepository accountRepository;
    private final JpaOrderRepository orderRepository;

    @Transactional(readOnly = true)
    public ShopStatisticsResponse getStatistics(
            Long shopId,
            ShopStatisticsPeriod period,
            String date,
            String month,
            String year
    ) {
        requireShopAccount(shopId);
        TimeRange range = resolveRange(period, date, month, year);
        List<OrderEntity> orders = period == ShopStatisticsPeriod.ALL
                ? orderRepository.findShopOrdersForAllTimeStatistics(shopId)
                : orderRepository.findShopOrdersForStatistics(shopId, range.startAt(), range.endAt());

        long totalOrders = orders.size();
        long cancelledOrders = orders.stream()
                .filter(order -> CANCELLED_STATUSES.contains(order.getOrderStatus()))
                .count();
        BigDecimal totalRevenue = orders.stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.COMPLETED)
                .map(OrderEntity::getFinalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        double cancellationRate = totalOrders == 0
                ? 0.0
                : BigDecimal.valueOf(cancelledOrders)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(totalOrders), 1, RoundingMode.HALF_UP)
                        .doubleValue();

        return ShopStatisticsResponse.builder()
                .period(period.name())
                .selectorText(range.selectorText())
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .cancelledOrders(cancelledOrders)
                .cancellationRate(cancellationRate)
                .entries(buildEntries(period, orders, range))
                .build();
    }

    private Account requireShopAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));
        if (!ROLE_SHOP.equals(account.getRole())) {
            throw new AccessDeniedException("Chỉ tài khoản chủ quán mới được dùng chức năng này");
        }
        return account;
    }

    private TimeRange resolveRange(ShopStatisticsPeriod period, String date, String month, String year) {
        return switch (period) {
            case DAY -> {
                LocalDate localDate = date == null || date.isBlank() ? LocalDate.now(APP_ZONE) : LocalDate.parse(date);
                yield TimeRange.forDate(
                        localDate,
                        localDate.plusDays(1),
                        localDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                );
            }
            case MONTH -> {
                YearMonth yearMonth = month == null || month.isBlank() ? YearMonth.now(APP_ZONE) : YearMonth.parse(month);
                yield TimeRange.forDate(
                        yearMonth.atDay(1),
                        yearMonth.plusMonths(1).atDay(1),
                        "Tháng %02d/%d".formatted(yearMonth.getMonthValue(), yearMonth.getYear())
                );
            }
            case YEAR -> {
                Year selectedYear = year == null || year.isBlank() ? Year.now(APP_ZONE) : Year.parse(year);
                yield TimeRange.forDate(
                        selectedYear.atDay(1),
                        selectedYear.plusYears(1).atDay(1),
                        "Năm %d".formatted(selectedYear.getValue())
                );
            }
            case ALL -> new TimeRange(null, null, "Toàn bộ");
        };
    }

    private List<ShopStatisticsResponse.RevenueEntryResponse> buildEntries(
            ShopStatisticsPeriod period,
            List<OrderEntity> orders,
            TimeRange range
    ) {
        Map<String, BigDecimal> buckets = switch (period) {
            case DAY -> dayBuckets();
            case MONTH -> monthBuckets(range.startAt().toLocalDate().lengthOfMonth());
            case YEAR -> yearBuckets();
            case ALL -> allBuckets(orders);
        };

        for (OrderEntity order : orders) {
            if (order.getOrderStatus() != OrderStatus.COMPLETED) {
                continue;
            }
            String key = entryKey(period, order);
            buckets.computeIfPresent(key, (ignored, value) -> value.add(order.getFinalAmount()));
        }

        return buckets.entrySet().stream()
                .map(entry -> ShopStatisticsResponse.RevenueEntryResponse.builder()
                        .label(entry.getKey())
                        .amount(entry.getValue())
                        .build())
                .toList();
    }

    private Map<String, BigDecimal> dayBuckets() {
        Map<String, BigDecimal> buckets = new LinkedHashMap<>();
        buckets.put("Sáng", BigDecimal.ZERO);
        buckets.put("Trưa", BigDecimal.ZERO);
        buckets.put("Chiều", BigDecimal.ZERO);
        buckets.put("Tối", BigDecimal.ZERO);
        return buckets;
    }

    private Map<String, BigDecimal> monthBuckets(int daysInMonth) {
        Map<String, BigDecimal> buckets = new LinkedHashMap<>();
        int step = 7;
        for (int start = 1; start <= daysInMonth; start += step) {
            int end = Math.min(start + step - 1, daysInMonth);
            buckets.put("%02d-%02d".formatted(start, end), BigDecimal.ZERO);
        }
        return buckets;
    }

    private Map<String, BigDecimal> yearBuckets() {
        Map<String, BigDecimal> buckets = new LinkedHashMap<>();
        for (int month = 1; month <= 12; month++) {
            buckets.put("T%d".formatted(month), BigDecimal.ZERO);
        }
        return buckets;
    }

    private Map<String, BigDecimal> allBuckets(List<OrderEntity> orders) {
        Map<String, BigDecimal> buckets = new LinkedHashMap<>();
        List<Integer> years = orders.stream()
                .map(order -> order.getCreatedAt().atZoneSameInstant(APP_ZONE).getYear())
                .distinct()
                .sorted()
                .toList();
        if (years.isEmpty()) {
            years = new ArrayList<>();
            years.add(Year.now(APP_ZONE).getValue());
        }
        years.forEach(year -> buckets.put(String.valueOf(year), BigDecimal.ZERO));
        return buckets;
    }

    private String entryKey(ShopStatisticsPeriod period, OrderEntity order) {
        var createdAt = order.getCreatedAt().atZoneSameInstant(APP_ZONE);
        return switch (period) {
            case DAY -> {
                int hour = createdAt.getHour();
                if (hour < 11) {
                    yield "Sáng";
                }
                if (hour < 14) {
                    yield "Trưa";
                }
                if (hour < 18) {
                    yield "Chiều";
                }
                yield "Tối";
            }
            case MONTH -> {
                int day = createdAt.getDayOfMonth();
                int start = ((day - 1) / 7) * 7 + 1;
                int end = Math.min(start + 6, createdAt.toLocalDate().lengthOfMonth());
                yield "%02d-%02d".formatted(start, end);
            }
            case YEAR -> "T%d".formatted(createdAt.getMonthValue());
            case ALL -> String.valueOf(createdAt.getYear());
        };
    }

    private record TimeRange(java.time.OffsetDateTime startAt, java.time.OffsetDateTime endAt, String selectorText) {
        private static TimeRange forDate(LocalDate startDate, LocalDate endDate, String selectorText) {
            return new TimeRange(
                    startDate.atStartOfDay(APP_ZONE).toOffsetDateTime(),
                    endDate.atStartOfDay(APP_ZONE).toOffsetDateTime(),
                    selectorText
            );
        }
    }
}
