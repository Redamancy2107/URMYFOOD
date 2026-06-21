package com.urmyfood.backend.infrastructure.persistence.repository;

import com.urmyfood.backend.infrastructure.persistence.entity.ChatSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface JpaChatSessionRepository extends JpaRepository<ChatSessionEntity, Long> {

    @Query("SELECT s FROM ChatSessionEntity s WHERE s.shop.id = :shopId AND s.user.id = :userId")
    Optional<ChatSessionEntity> findByShopIdAndUserId(@Param("shopId") Long shopId, @Param("userId") Long userId);

    @Modifying
    @Query("UPDATE ChatSessionEntity s SET s.updatedAt = :now WHERE s.id = :sessionId")
    void touchUpdatedAt(@Param("sessionId") Long sessionId, @Param("now") LocalDateTime now);

    List<ChatSessionEntity> findByShopIdOrderByUpdatedAtDesc(Long shopId);

    List<ChatSessionEntity> findByUserIdOrderByUpdatedAtDesc(Long userId);

    @Query("SELECT s FROM ChatSessionEntity s WHERE s.shop.id = :shopId OR s.user.id = :userId ORDER BY s.updatedAt DESC")
    List<ChatSessionEntity> findByParticipant(@Param("shopId") Long shopId, @Param("userId") Long userId);
}
