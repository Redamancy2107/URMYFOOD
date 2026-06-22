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
public class ShopFollowResponse {
    @JsonProperty("shop_id")
    private Long shopId;

    @JsonProperty("is_following")
    private boolean isFollowing;

    @JsonProperty("follower_count")
    private long followerCount;
}
