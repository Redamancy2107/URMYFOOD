package com.urmyfood.backend.infrastructure.persistence.entity;

import com.urmyfood.backend.domain.model.PostStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "post_id", updatable = false, nullable = false)
    private UUID postId;

    @Column(name = "dish_name", nullable = false)
    private String dishName;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "original_price", nullable = false)
    private BigDecimal originalPrice;

    @Column(name = "max_quantity", nullable = false)
    private int maxQuantity;

    @Column(name = "remaining_quantity", nullable = false)
    private int remainingQuantity;

    @Column(name = "end_time")
    private OffsetDateTime endTime;

    @Column(name = "is_flash_sale", nullable = false)
    private boolean flashSale;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PostStatus status;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "category")
    private String category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private AccountEntity author;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}
