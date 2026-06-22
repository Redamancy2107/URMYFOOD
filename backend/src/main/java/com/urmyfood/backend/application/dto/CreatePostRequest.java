package com.urmyfood.backend.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreatePostRequest {

    @NotBlank
    @JsonProperty("dish_name")
    private String dishName;

    @NotNull
    @DecimalMin(value = "0.01", message = "Giá món ăn phải lớn hơn 0")
    private BigDecimal price;

    @DecimalMin("0.0")
    @JsonProperty("original_price")
    private BigDecimal originalPrice;

    @Min(1)
    @JsonProperty("max_quantity")
    private int maxQuantity;

    @JsonProperty("end_time")
    private OffsetDateTime endTime;

    @JsonProperty("is_flash_sale")
    private boolean isFlashSale;

    private String content;

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("category")
    private String category;
}
