package com.urmyfood.backend.application.dto;

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
public class DashboardOverviewResponse {
    private BigDecimal totalRevenue;
    private long newOrders;
    private long newUsers;
    private long activeShops;
    private List<MonthlyRevenue> monthlyRevenueGrowth;
    private List<RecentActivity> recentActivities;
    private List<LatestShop> latestShops;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MonthlyRevenue {
        private int month;
        private BigDecimal revenue;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RecentActivity {
        private String type;
        private String description;
        private String time;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LatestShop {
        private Long id;
        private String shopName;
        private String email;
        private String status;
        private String createdAt;
    }
}
