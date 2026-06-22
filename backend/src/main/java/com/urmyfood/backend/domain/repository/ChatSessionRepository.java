package com.urmyfood.backend.domain.repository;

import com.urmyfood.backend.domain.model.ChatSession;

import java.util.List;
import java.util.Optional;

public interface ChatSessionRepository {
    ChatSession save(ChatSession session);
    Optional<ChatSession> findById(Long id);
    Optional<ChatSession> findByShopIdAndUserId(Long shopId, Long userId);
    List<ChatSession> findByShopId(Long shopId);
    List<ChatSession> findByUserId(Long userId);
    void touchUpdatedAt(Long sessionId);
}
