package com.urmyfood.backend.presentation.controller;

import com.urmyfood.backend.application.dto.ApiResponse;
import com.urmyfood.backend.application.dto.ChatMessageResponse;
import com.urmyfood.backend.application.dto.ChatSessionResponse;
import com.urmyfood.backend.application.dto.GetOrCreateSessionRequest;
import com.urmyfood.backend.application.service.ChatService;
import com.urmyfood.backend.infrastructure.security.CustomAccountDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<List<ChatSessionResponse>>> getSessions(Authentication authentication) {
        Long accountId = getAccountId(authentication);
        String role = getRole(authentication);
        List<ChatSessionResponse> sessions = role.contains("SHOP")
                ? chatService.getSessionsByShop(accountId)
                : chatService.getSessionsByUser(accountId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách chat thành công", sessions));
    }

    @PostMapping("/sessions")
    public ResponseEntity<ApiResponse<ChatSessionResponse>> getOrCreateSession(
            Authentication authentication,
            @Valid @RequestBody GetOrCreateSessionRequest request
    ) {
        Long userId = getAccountId(authentication);
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy hoặc tạo session thành công",
                chatService.getOrCreateSession(request.getShopId(), userId)
        ));
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getMessages(
            Authentication authentication,
            @PathVariable Long sessionId
    ) {
        Long accountId = getAccountId(authentication);
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy tin nhắn thành công",
                chatService.getMessages(sessionId, accountId)
        ));
    }

    @PutMapping("/sessions/{sessionId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            Authentication authentication,
            @PathVariable Long sessionId
    ) {
        Long accountId = getAccountId(authentication);
        chatService.markAsRead(sessionId, accountId);
        return ResponseEntity.ok(ApiResponse.success("Đã đánh dấu đã đọc", null));
    }

    private Long getAccountId(Authentication authentication) {
        return ((CustomAccountDetails) authentication.getPrincipal()).getAccount().getId();
    }

    private String getRole(Authentication authentication) {
        return ((CustomAccountDetails) authentication.getPrincipal()).getAccount().getRole();
    }
}
