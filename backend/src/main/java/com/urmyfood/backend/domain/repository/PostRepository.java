package com.urmyfood.backend.domain.repository;

import com.urmyfood.backend.application.dto.UpdatePostRequest;
import com.urmyfood.backend.domain.model.Post;
import com.urmyfood.backend.domain.model.PostComment;
import com.urmyfood.backend.domain.model.PostRanked;
import com.urmyfood.backend.domain.model.PostStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostRepository {
    Post save(Post post);
    Optional<PostRanked> findRankedPostById(UUID postId, Long viewerAccountId);
    Optional<Post> findById(UUID postId);
    boolean existsById(UUID postId);
    Optional<Post> findByIdForUpdate(UUID postId);
    List<Post> findAllOrderedByCreatedAt();

    // Anchored offset for newsfeed
    List<PostRanked> findRanked(Long viewerAccountId, double w1, double w2, double w3, int page, int size, OffsetDateTime anchor);
    long countActive(OffsetDateTime anchor);

    // Anchored offset for search
    List<PostRanked> searchByKeyword(String keyword, Long viewerAccountId, int page, int size, OffsetDateTime anchor);
    long countByKeyword(String keyword, OffsetDateTime anchor);

    long likePost(UUID postId, Long accountId);
    long unlikePost(UUID postId, Long accountId);

    // Cursor-based for comments
    List<PostComment> findComments(UUID postId, OffsetDateTime cursor, int size);
    long countComments(UUID postId);
    PostComment saveComment(UUID postId, Long accountId, String content, UUID parentId);

    // Shop owner post management
    List<PostRanked> findByAuthorId(Long accountId, int page, int size);
    long countByAuthorId(Long accountId);
    void updatePost(UUID postId, Long authorId, UpdatePostRequest req);
    void updateRemainingQuantity(UUID postId, Long authorId, int remainingQuantity);
    void deletePost(UUID postId, Long authorId);
    void updatePostStatus(UUID postId, Long authorId, PostStatus status);

    // Admin methods
    List<Post> findAll(int page, int size);
    long countAll();
    void adminUpdatePostStatus(UUID postId, PostStatus status);
    void adminDeletePost(UUID postId);
}
