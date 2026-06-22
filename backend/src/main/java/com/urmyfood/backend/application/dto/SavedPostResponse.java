package com.urmyfood.backend.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SavedPostResponse {
    @JsonProperty("post_id")
    private UUID postId;

    @JsonProperty("is_saved")
    private boolean isSaved;
}
