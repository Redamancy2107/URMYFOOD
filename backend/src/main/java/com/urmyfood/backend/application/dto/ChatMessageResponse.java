package com.urmyfood.backend.application.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatMessageResponse {
    private Long id;
    private Long sessionId;
    private Long senderId;
    private String senderRole;
    private String content;
    private boolean read;
    private String sentAt;
}
