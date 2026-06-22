package com.urmyfood.backend.application.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatSessionResponse {
    private Long id;
    private Long shopId;
    private String shopName;
    private String shopAvatarUrl;
    private Long userId;
    private String customerName;
    private String customerAvatarUrl;
    private String lastMessage;
    private String lastMessageAt;
    private int unreadCount;
}
