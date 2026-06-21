package com.urmyfood.backend.infrastructure.persistence.repository;

import com.urmyfood.backend.infrastructure.persistence.entity.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JpaChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {

    List<ChatMessageEntity> findBySessionIdOrderBySentAtAsc(Long sessionId);

    Optional<ChatMessageEntity> findTopBySessionIdOrderBySentAtDesc(Long sessionId);

    long countBySessionIdAndReadFalseAndSenderIdNot(Long sessionId, Long senderId);

    @Modifying
    @Query("UPDATE ChatMessageEntity m SET m.read = true WHERE m.session.id = :sessionId AND m.sender.id <> :readerId AND m.read = false")
    void markAllAsRead(@Param("sessionId") Long sessionId, @Param("readerId") Long readerId);
}
