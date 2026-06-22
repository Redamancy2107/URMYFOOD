package com.urmyfood.backend.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopStatisticsResponse {
    private String period;
    @JsonProperty("selector_text")
    private String selectorText;
    @JsonProperty("total_revenue")
    private BigDecimal totalRevenue;
    @JsonProperty("total_orders")
    private long totalOrders;
    @JsonProperty("cancelled_orders")
    private long cancelledOrders;
    @JsonProperty("cancellation_rate")
    private double cancellationRate;
    private List<RevenueEntryResponse> entries;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueEntryResponse {
        private String label;
        private BigDecimal amount;
    }
}
