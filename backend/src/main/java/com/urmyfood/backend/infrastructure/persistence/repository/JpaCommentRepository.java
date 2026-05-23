package com.urmyfood.backend.infrastructure.persistence.repository;

import com.urmyfood.backend.infrastructure.persistence.entity.CommentEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaCommentRepository extends JpaRepository<CommentEntity, UUID> {

    @EntityGraph(attributePaths = {"account", "post"})
    List<CommentEntity> findByPost_PostId(UUID postId, Pageable pageable);

    long countByPost_PostId(UUID postId);
}
