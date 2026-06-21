package com.urmyfood.backend.infrastructure.persistence.adapter;

import com.urmyfood.backend.domain.model.ChatSession;
import com.urmyfood.backend.domain.repository.ChatSessionRepository;
import com.urmyfood.backend.infrastructure.persistence.entity.AccountEntity;
import com.urmyfood.backend.infrastructure.persistence.entity.ChatSessionEntity;
import com.urmyfood.backend.infrastructure.persistence.repository.JpaAccountRepository;
import com.urmyfood.backend.infrastructure.persistence.repository.JpaChatSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ChatSessionPersistenceAdapter implements ChatSessionRepository {

    private final JpaChatSessionRepository jpaRepository;
    private final JpaAccountRepository jpaAccountRepository;

    @Override
    public ChatSession save(ChatSession session) {
        AccountEntity shop = jpaAccountRepository.findById(session.getShopId())
                .orElseThrow(() -> new RuntimeException("Shop not found: " + session.getShopId()));
        AccountEntity user = jpaAccountRepository.findById(session.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found: " + session.getUserId()));
        ChatSessionEntity entity = ChatSessionEntity.builder()
                .id(session.getId())
                .shop(shop)
                .user(user)
                .build();
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<ChatSession> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<ChatSession> findByShopIdAndUserId(Long shopId, Long userId) {
        return jpaRepository.findByShopIdAndUserId(shopId, userId).map(this::toDomain);
    }

    @Override
    public List<ChatSession> findByShopId(Long shopId) {
        return jpaRepository.findByShopIdOrderByUpdatedAtDesc(shopId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public List<ChatSession> findByUserId(Long userId) {
        return jpaRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    @Transactional
    public void touchUpdatedAt(Long sessionId) {
        jpaRepository.touchUpdatedAt(sessionId, LocalDateTime.now());
    }

    public ChatSession toDomain(ChatSessionEntity entity) {
        return ChatSession.builder()
                .id(entity.getId())
                .shopId(entity.getShop().getId())
                .userId(entity.getUser().getId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
