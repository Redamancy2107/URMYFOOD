package com.urmyfood.backend.application.dto;

import lombok.Data;

@Data
public class ReportBlockShopRequest {
    private int blockDays;
    private String reason;
}
