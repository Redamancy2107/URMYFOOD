package com.urmyfood.backend.application.service;

import com.urmyfood.backend.application.dto.CommentResponse;
import com.urmyfood.backend.application.dto.CreateCommentRequest;
import com.urmyfood.backend.application.dto.LikeToggleResponse;
import com.urmyfood.backend.application.dto.CreatePostRequest;
import com.urmyfood.backend.application.dto.PageResponse;
import com.urmyfood.backend.application.dto.PostResponse;
import com.urmyfood.backend.domain.model.Account;
import com.urmyfood.backend.domain.model.Post;
import com.urmyfood.backend.domain.model.PostComment;
import com.urmyfood.backend.domain.model.PostRanked;
import com.urmyfood.backend.domain.model.PostStatus;
import com.urmyfood.backend.domain.repository.AccountRepository;
import com.urmyfood.backend.domain.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final AccountRepository accountRepository;

    @Value("${recommendation.weight.likes:1.0}")
    private double wLikes;

    @Value("${recommendation.weight.comments:0.5}")
    private double wComments;

    @Value("${recommendation.weight.recency:100.0}")
    private double wRecency;

    public PageResponse<PostResponse> getNewsfeed(int page, int size) {
        int clampedSize = Math.min(size, 50);
        Long viewerAccountId = resolveViewerAccountId();
        List<PostRanked> ranked = postRepository.findRanked(viewerAccountId, wLikes, wComments, wRecency, page, clampedSize);
        long total = postRepository.countActive();
        List<PostResponse> content = ranked.stream().map(this::toResponse).toList();
        return PageResponse.of(content, page, clampedSize, total);
    }

    public PageResponse<PostResponse> searchPosts(String keyword, int page, int size) {
        int clampedSize = Math.min(size, 50);
        Long viewerAccountId = resolveViewerAccountId();
        List<PostRanked> results = postRepository.searchByKeyword(keyword, viewerAccountId, page, clampedSize);
        long total = postRepository.countByKeyword(keyword);
        List<PostResponse> content = results.stream().map(this::toResponse).toList();
        return PageResponse.of(content, page, clampedSize, total);
    }

    public PostResponse createPost(CreatePostRequest request) {
        Account author = requireCurrentAccount();

        Post post = Post.builder()
                .dishName(request.getDishName())
                .price(request.getPrice())
                .originalPrice(request.getOriginalPrice())
                .maxQuantity(request.getMaxQuantity())
                .remainingQuantity(request.getMaxQuantity())
                .endTime(request.getEndTime())
                .isFlashSale(request.isFlashSale())
                .status(PostStatus.ACTIVE)
                .content(request.getContent())
                .imageUrl(request.getImageUrl())
                .author(author)
                .build();

        Post saved = postRepository.save(post);
        return PostResponse.builder()
                .postId(saved.getPostId())
                .dishName(saved.getDishName())
                .price(saved.getPrice())
                .originalPrice(saved.getOriginalPrice())
                .maxQuantity(saved.getMaxQuantity())
                .remainingQuantity(saved.getRemainingQuantity())
                .endTime(saved.getEndTime())
                .isFlashSale(saved.isFlashSale())
                .status(saved.getStatus().name())
                .content(saved.getContent())
                .imageUrl(saved.getImageUrl())
                .shopName(saved.getAuthor().getFullName())
                .shopAvatarUrl(saved.getAuthor().getAvatarUrl())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    public LikeToggleResponse likePost(UUID postId) {
        Account account = requireCurrentAccount();
        ensurePostExists(postId);
        long likeCount = postRepository.likePost(postId, account.getId());
        return LikeToggleResponse.builder()
                .likeCount(likeCount)
                .isLiked(true)
                .build();
    }

    public LikeToggleResponse unlikePost(UUID postId) {
        Account account = requireCurrentAccount();
        ensurePostExists(postId);
        long likeCount = postRepository.unlikePost(postId, account.getId());
        return LikeToggleResponse.builder()
                .likeCount(likeCount)
                .isLiked(false)
                .build();
    }

    public PageResponse<CommentResponse> getComments(UUID postId, int page, int size) {
        ensurePostExists(postId);
        int clampedSize = Math.min(size, 50);
        List<CommentResponse> comments = postRepository.findComments(postId, page, clampedSize)
                .stream()
                .map(this::toCommentResponse)
                .toList();
        long total = postRepository.countComments(postId);
        return PageResponse.of(comments, page, clampedSize, total);
    }

    public CommentResponse createComment(UUID postId, CreateCommentRequest request) {
        Account account = requireCurrentAccount();
        ensurePostExists(postId);
        String content = request.getContent() != null ? request.getContent().trim() : "";
        if (content.isBlank()) {
            throw new IllegalArgumentException("Content must not be blank");
        }
        return toCommentResponse(postRepository.saveComment(postId, account.getId(), content));
    }

    private PostResponse toResponse(PostRanked pr) {
        return PostResponse.builder()
                .postId(pr.postId())
                .dishName(pr.dishName())
                .price(pr.price())
                .originalPrice(pr.originalPrice())
                .maxQuantity(pr.maxQuantity())
                .remainingQuantity(pr.remainingQuantity())
                .endTime(pr.endTime())
                .isFlashSale(pr.flashSale())
                .status(pr.status().name())
                .content(pr.content())
                .imageUrl(pr.imageUrl())
                .shopName(pr.shopName())
                .shopAvatarUrl(pr.shopAvatarUrl())
                .createdAt(pr.createdAt())
                .likeCount(pr.likeCount())
                .isLiked(pr.liked())
                .commentCount(pr.commentCount())
                .build();
    }

    private CommentResponse toCommentResponse(PostComment comment) {
        return CommentResponse.builder()
                .commentId(comment.commentId())
                .authorName(comment.authorName())
                .authorAvatarUrl(comment.authorAvatarUrl())
                .content(comment.content())
                .createdAt(comment.createdAt())
                .build();
    }

    private Account requireCurrentAccount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new AccessDeniedException("Not authenticated");
        }
        return accountRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }

    private void ensurePostExists(UUID postId) {
        if (postRepository.findById(postId).isEmpty()) {
            throw new RuntimeException("Post not found");
        }
    }

    private Long resolveViewerAccountId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return accountRepository.findByEmail(auth.getName())
                .map(Account::getId)
                .orElse(null);
    }
}
