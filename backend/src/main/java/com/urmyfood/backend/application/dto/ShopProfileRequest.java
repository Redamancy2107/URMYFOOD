package com.urmyfood.backend.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopProfileRequest {
    @JsonProperty("shop_name")
    private String shopName;
    @JsonProperty("logo_url")
    private String logoUrl;
    @JsonProperty("cover_url")
    private String coverUrl;
    private String category;
    private String address;
    private Double latitude;
    private Double longitude;
    private String description;
    @JsonProperty("opening_hours")
    private String openingHours;
    @JsonProperty("is_open")
    private Boolean isOpen;
}
