package com.urmyfood.backend.domain.repository;

import com.urmyfood.backend.domain.model.Comment;

import java.util.List;
import java.util.UUID;

public interface CommentRepository {
    Comment save(Comment comment);
    List<Comment> findByPostId(UUID postId);
    long countByPostId(UUID postId);
}
