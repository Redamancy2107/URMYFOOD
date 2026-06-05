package com.urmyfood.backend.infrastructure.persistence.entity;

import com.urmyfood.backend.domain.model.ShopVerificationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "shop_verifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ShopVerificationEntity extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false, unique = true)
    private AccountEntity shop;

    @Column(name = "shop_name", nullable = false)
    private String shopName;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;

    private Double latitude;

    private Double longitude;

    @Column(name = "cccd_front_url", nullable = false, columnDefinition = "TEXT")
    private String cccdFrontUrl;

    @Column(name = "cccd_back_url", nullable = false, columnDefinition = "TEXT")
    private String cccdBackUrl;

    @Column(name = "shop_photo_urls", nullable = false, columnDefinition = "TEXT")
    private String shopPhotoUrls;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ShopVerificationStatus status;

    @Column(name = "reject_reason", columnDefinition = "TEXT")
    private String rejectReason;
}
