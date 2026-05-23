package com.urmyfood.backend.domain.model;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Like {
    private UUID likeId;
    private Post post;
    private Account account;
    private OffsetDateTime createdAt;
}
