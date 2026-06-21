package com.urmyfood.backend.presentation.controller;

import com.urmyfood.backend.application.dto.ChatMessageResponse;
import com.urmyfood.backend.application.dto.SendMessageRequest;
import com.urmyfood.backend.application.service.ChatService;
import com.urmyfood.backend.infrastructure.security.CustomAccountDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(@Valid SendMessageRequest request, Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomAccountDetails)) {
            // Reject unauthenticated or anonymous STOMP SEND frames silently.
            return;
        }
        CustomAccountDetails details = (CustomAccountDetails) authentication.getPrincipal();
        Long senderId = details.getAccount().getId();
        String role = details.getAccount().getRole();
        String senderRole = role.contains("SHOP") ? "SHOP" : "CUSTOMER";

        ChatMessageResponse response = chatService.sendMessage(
                request.getSessionId(), senderId, request.getContent(), senderRole
        );
        messagingTemplate.convertAndSend("/topic/chat/" + request.getSessionId(), response);
    }
}
