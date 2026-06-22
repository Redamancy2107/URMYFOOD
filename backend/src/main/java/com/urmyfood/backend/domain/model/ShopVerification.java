package com.urmyfood.backend.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ShopVerification extends BaseDomainModel {
    private Account shop;
    private String shopName;
    private String category;
    private String address;
    private Double latitude;
    private Double longitude;
    private String cccdFrontUrl;
    private String cccdBackUrl;
    private List<String> shopPhotoUrls;
    private ShopVerificationStatus status;
    private String rejectReason;
}
