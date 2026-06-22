package com.urmyfood.backend.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {
    private UUID reportId;
    private UUID postId;
    private String postContent;
    private String reporterName;
    private String reason;
    private String status;
    private OffsetDateTime createdAt;
}
