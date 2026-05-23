package com.urmyfood.backend.domain.repository;

import com.urmyfood.backend.domain.model.Like;

import java.util.UUID;

public interface LikeRepository {
    void save(Like like);
    void delete(UUID postId, Long accountId);
    boolean existsByPostIdAndAccountId(UUID postId, Long accountId);
    long countByPostId(UUID postId);
}
