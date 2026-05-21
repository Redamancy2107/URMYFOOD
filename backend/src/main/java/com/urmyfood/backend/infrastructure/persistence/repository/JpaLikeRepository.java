package com.urmyfood.backend.infrastructure.persistence.repository;

import com.urmyfood.backend.infrastructure.persistence.entity.LikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

public interface JpaLikeRepository extends JpaRepository<LikeEntity, UUID> {
    Optional<LikeEntity> findByPost_PostIdAndAccount_Id(UUID postId, Long accountId);
    boolean existsByPost_PostIdAndAccount_Id(UUID postId, Long accountId);
    long countByPost_PostId(UUID postId);

    @Transactional
    void deleteByPost_PostIdAndAccount_Id(UUID postId, Long accountId);
}
