package com.urmyfood.backend.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountActionLog {
    private Long id;
    private String targetType; // "ACCOUNT", "POST", "REPORT", "SHOP"
    private String targetIdStr; // string representation of id
    private String actionType; // "LOCK", "UNLOCK", "DELETE", "APPROVE", "REJECT"
    private String reason;
    private Instant createdAt;
}
