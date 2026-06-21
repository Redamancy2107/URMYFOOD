package com.urmyfood.backend.presentation.controller;

import com.urmyfood.backend.application.dto.ApiResponse;
import com.urmyfood.backend.application.dto.ChatMessageResponse;
import com.urmyfood.backend.application.dto.ChatSessionResponse;
import com.urmyfood.backend.application.dto.GetOrCreateSessionRequest;
import com.urmyfood.backend.application.service.ChatImageStorageClient;
import com.urmyfood.backend.application.service.ChatService;
import com.urmyfood.backend.infrastructure.security.CustomAccountDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;
    private final ChatImageStorageClient chatImageStorageClient;

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

    @PostMapping("/sessions/{sessionId}/upload-image")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadChatImage(
            Authentication authentication,
            @PathVariable Long sessionId,
            @RequestParam("file") MultipartFile file
    ) {
        Long accountId = getAccountId(authentication);
        log.info("[CHAT IMAGE] upload request sessionId={} accountId={} contentType={} size={}",
                sessionId, accountId, file.getContentType(), file.getSize());
        String imageUrl = chatImageStorageClient.uploadChatImage(sessionId, file);
        return ResponseEntity.ok(ApiResponse.success("Tải ảnh thành công", Map.of("imageUrl", imageUrl)));
    }

    private Long getAccountId(Authentication authentication) {
        return ((CustomAccountDetails) authentication.getPrincipal()).getAccount().getId();
    }

    private String getRole(Authentication authentication) {
        return ((CustomAccountDetails) authentication.getPrincipal()).getAccount().getRole();
    }
}
