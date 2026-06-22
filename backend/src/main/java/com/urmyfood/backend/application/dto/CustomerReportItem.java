package com.urmyfood.backend.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerReportItem {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private BigDecimal totalSpent;
    private long totalOrders;
    private long completedOrders;
    private String createdAt;
}
