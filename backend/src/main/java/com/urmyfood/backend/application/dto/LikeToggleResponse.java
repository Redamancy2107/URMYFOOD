package com.urmyfood.backend.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LikeToggleResponse {

    @JsonProperty("like_count")
    private long likeCount;

    @JsonProperty("is_liked")
    private boolean isLiked;
}
