package com.urmyfood.backend.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ChatMessage {
    private Long id;
    private Long sessionId;
    private Long senderId;
    private String senderRole;
    private String messageType;
    private String content;
    private String imageUrl;
    private boolean isRead;
    private OffsetDateTime sentAt;
}
