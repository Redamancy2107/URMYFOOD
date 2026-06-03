package com.urmyfood.backend.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShopVerificationResponse {
    private Long id;

    @JsonProperty("shop_id")
    private Long shopId;

    @JsonProperty("shop_name")
    private String shopName;

    private String category;
    private String address;
    private Double latitude;
    private Double longitude;

    @JsonProperty("cccd_front_url")
    private String cccdFrontUrl;

    @JsonProperty("cccd_back_url")
    private String cccdBackUrl;

    @JsonProperty("shop_photo_urls")
    private List<String> shopPhotoUrls;

    private String status;

    @JsonProperty("reject_reason")
    private String rejectReason;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
