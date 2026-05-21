package com.urmyfood.backend.infrastructure.persistence.repository;

import com.urmyfood.backend.infrastructure.persistence.entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaCommentRepository extends JpaRepository<CommentEntity, UUID> {
    List<CommentEntity> findByPost_PostIdOrderByCreatedAtAsc(UUID postId);
    long countByPost_PostId(UUID postId);
}
