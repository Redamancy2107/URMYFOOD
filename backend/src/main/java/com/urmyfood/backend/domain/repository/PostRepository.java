package com.urmyfood.backend.domain.repository;

import com.urmyfood.backend.domain.model.Post;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostRepository {
    Post save(Post post);
    Optional<Post> findById(UUID postId);
    List<Post> findAllOrderedByCreatedAt();
}
