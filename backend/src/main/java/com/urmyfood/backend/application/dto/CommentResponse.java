package com.urmyfood.backend.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponse {

    @JsonProperty("comment_id")
    private UUID commentId;

    @JsonProperty("author_name")
    private String authorName;

    @JsonProperty("author_avatar_url")
    private String authorAvatarUrl;

    private String content;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    @JsonProperty("parent_id")
    private UUID parentId;
}
