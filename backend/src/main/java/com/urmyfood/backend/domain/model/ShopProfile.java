package com.urmyfood.backend.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ShopProfile extends BaseDomainModel {
    private Account shop;
    private String shopName;
    private String logoUrl;
    private String coverUrl;
    private String category;
    private String address;
    private Double latitude;
    private Double longitude;
    private String description;
    private String openingHours;
    private Boolean isOpen;
    private String verificationStatus;
}
