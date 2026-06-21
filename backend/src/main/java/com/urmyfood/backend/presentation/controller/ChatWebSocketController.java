package com.urmyfood.backend.presentation.controller;

import com.urmyfood.backend.application.dto.ChatMessageResponse;
import com.urmyfood.backend.application.dto.SendMessageRequest;
import com.urmyfood.backend.application.service.ChatService;
import com.urmyfood.backend.infrastructure.security.CustomAccountDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(@Valid SendMessageRequest request, Authentication authentication) {
        log.info("[WS] sendMessage called — auth={}", authentication != null ? authentication.getName() : "null");
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomAccountDetails)) {
            log.warn("[WS] Rejected: unauthenticated STOMP SEND");
            // Reject unauthenticated or anonymous STOMP SEND frames silently.
            return;
        }
        CustomAccountDetails details = (CustomAccountDetails) authentication.getPrincipal();
        Long senderId = details.getAccount().getId();
        String role = details.getAccount().getRole();
        String senderRole = role.contains("SHOP") ? "SHOP" : "CUSTOMER";

        ChatMessageResponse response = chatService.sendMessage(
                request.getSessionId(), senderId, request.getContent(), senderRole,
                request.getMessageType(), request.getImageUrl()
        );
        String destination = "/topic/chat/" + request.getSessionId();
        log.info("[WS] Broadcasting to {} — msgId={}", destination, response.getId());
        messagingTemplate.convertAndSend(destination, response);
        log.info("[WS] Broadcast done for msgId={}", response.getId());
    }
}
