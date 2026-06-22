package com.urmyfood.backend.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {
    private UUID reportId;
    private Post post;
    private Account reporter;
    private String reason;
    private ReportStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime resolvedAt;
    private Account resolvedBy;
}
