package com.urmyfood.backend.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SendMessageRequest {

    @NotNull
    private Long sessionId;

    private String content;

    private String messageType = "TEXT";

    private String imageUrl;
}
