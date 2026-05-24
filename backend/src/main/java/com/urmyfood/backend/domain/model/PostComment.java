package com.urmyfood.backend.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PostComment(
        UUID commentId,
        String authorName,
        String authorAvatarUrl,
        String content,
        OffsetDateTime createdAt
) {
}
