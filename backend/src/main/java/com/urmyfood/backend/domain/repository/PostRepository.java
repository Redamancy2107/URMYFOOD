package com.urmyfood.backend.domain.repository;

import com.urmyfood.backend.domain.model.Post;
import com.urmyfood.backend.domain.model.PostComment;
import com.urmyfood.backend.domain.model.PostRanked;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostRepository {
    Post save(Post post);
    Optional<Post> findById(UUID postId);
    List<Post> findAllOrderedByCreatedAt();

    List<PostRanked> findRanked(Long viewerAccountId, double w1, double w2, double w3, int page, int size);
    long countActive();
    List<PostRanked> searchByKeyword(String keyword, Long viewerAccountId, int page, int size);
    long countByKeyword(String keyword);
    long likePost(UUID postId, Long accountId);
    long unlikePost(UUID postId, Long accountId);
    List<PostComment> findComments(UUID postId, int page, int size);
    long countComments(UUID postId);
    PostComment saveComment(UUID postId, Long accountId, String content);
}
