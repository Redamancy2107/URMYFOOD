package com.urmyfood.backend.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "account_action_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountActionLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "target_type", length = 30)
    private String targetType;

    @Column(name = "target_id_str", nullable = false)
    private String targetIdStr;

    @Column(name = "action_type", nullable = false, length = 30)
    private String actionType;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
