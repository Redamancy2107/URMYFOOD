package com.urmyfood.backend.domain.repository;

import com.urmyfood.backend.domain.model.ChatMessage;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository {
    ChatMessage save(ChatMessage message);
    List<ChatMessage> findBySessionId(Long sessionId);
    Optional<ChatMessage> findLastBySessionId(Long sessionId);
    long countUnread(Long sessionId, Long senderId);
    void markAllAsRead(Long sessionId, Long readerId);
}
