package com.urmyfood.backend.infrastructure.persistence.adapter;

import com.urmyfood.backend.domain.model.ChatMessage;
import com.urmyfood.backend.domain.repository.ChatMessageRepository;
import com.urmyfood.backend.infrastructure.persistence.entity.AccountEntity;
import com.urmyfood.backend.infrastructure.persistence.entity.ChatMessageEntity;
import com.urmyfood.backend.infrastructure.persistence.entity.ChatSessionEntity;
import com.urmyfood.backend.infrastructure.persistence.repository.JpaAccountRepository;
import com.urmyfood.backend.infrastructure.persistence.repository.JpaChatMessageRepository;
import com.urmyfood.backend.infrastructure.persistence.repository.JpaChatSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ChatMessagePersistenceAdapter implements ChatMessageRepository {

    private final JpaChatMessageRepository jpaRepository;
    private final JpaChatSessionRepository jpaSessionRepository;
    private final JpaAccountRepository jpaAccountRepository;

    @Override
    public ChatMessage save(ChatMessage message) {
        ChatSessionEntity session = jpaSessionRepository.findById(message.getSessionId())
                .orElseThrow(() -> new RuntimeException("Session not found: " + message.getSessionId()));
        AccountEntity sender = jpaAccountRepository.findById(message.getSenderId())
                .orElseThrow(() -> new RuntimeException("Sender not found: " + message.getSenderId()));
        ChatMessageEntity entity = ChatMessageEntity.builder()
                .session(session)
                .sender(sender)
                .senderRole(message.getSenderRole())
                .content(message.getContent())
                .read(message.isRead())
                .build();
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public List<ChatMessage> findBySessionId(Long sessionId) {
        return jpaRepository.findBySessionIdOrderBySentAtAsc(sessionId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public Optional<ChatMessage> findLastBySessionId(Long sessionId) {
        return jpaRepository.findTopBySessionIdOrderBySentAtDesc(sessionId).map(this::toDomain);
    }

    @Override
    public long countUnread(Long sessionId, Long senderId) {
        return jpaRepository.countBySessionIdAndReadFalseAndSenderIdNot(sessionId, senderId);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long sessionId, Long readerId) {
        jpaRepository.markAllAsRead(sessionId, readerId);
    }

    public ChatMessage toDomain(ChatMessageEntity entity) {
        return ChatMessage.builder()
                .id(entity.getId())
                .sessionId(entity.getSession().getId())
                .senderId(entity.getSender().getId())
                .senderRole(entity.getSenderRole())
                .content(entity.getContent())
                .isRead(entity.isRead())
                .sentAt(entity.getSentAt())
                .build();
    }
}
