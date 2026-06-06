package com.urmyfood.backend.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShopVerificationRequest {

    @NotBlank
    @JsonProperty("shop_name")
    private String shopName;

    @NotBlank
    private String category;

    @NotBlank
    private String address;

    private Double latitude;

    private Double longitude;

    @NotBlank
    @JsonProperty("cccd_front_url")
    private String cccdFrontUrl;

    @NotBlank
    @JsonProperty("cccd_back_url")
    private String cccdBackUrl;

    @NotEmpty
    @JsonProperty("shop_photo_urls")
    private List<String> shopPhotoUrls;
}
