package com.urmyfood.backend.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SendMessageRequest {

    @NotNull
    private Long sessionId;

    @NotBlank
    private String content;
}
