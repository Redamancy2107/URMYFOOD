package com.urmyfood.backend.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReorderResponse {
    @JsonProperty("added_count")
    private int addedCount;

    @JsonProperty("skipped_items")
    private List<SkippedItem> skippedItems;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkippedItem {
        @JsonProperty("post_id")
        private UUID postId;

        @JsonProperty("dish_name")
        private String dishName;

        private String reason;
    }
}
