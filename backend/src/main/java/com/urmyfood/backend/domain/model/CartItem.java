package com.urmyfood.backend.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {
    private UUID cartItemId;
    private Account customer;
    private Post post;
    private int quantity;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
