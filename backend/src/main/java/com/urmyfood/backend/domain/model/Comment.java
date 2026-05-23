package com.urmyfood.backend.domain.model;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {
    private UUID commentId;
    private Post post;
    private Account account;
    private String content;
    private OffsetDateTime createdAt;
}
