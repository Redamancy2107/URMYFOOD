package com.urmyfood.backend.application.service;

import com.urmyfood.backend.application.dto.CommentResponse;
import com.urmyfood.backend.application.dto.CreateCommentRequest;
import com.urmyfood.backend.application.dto.LikeToggleResponse;
import com.urmyfood.backend.application.dto.CreatePostRequest;
import com.urmyfood.backend.application.dto.PageResponse;
import com.urmyfood.backend.application.dto.PostImageUploadResponse;
import com.urmyfood.backend.application.dto.PostResponse;
import com.urmyfood.backend.application.dto.SavedPostResponse;
import com.urmyfood.backend.application.dto.UpdatePostRequest;
import com.urmyfood.backend.application.dto.UpdatePostStatusRequest;
import com.urmyfood.backend.application.dto.UpdateRemainingQuantityRequest;
import com.urmyfood.backend.application.service.PostImageStorageClient;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final AccountRepository accountRepository;
    private final PostImageStorageClient postImageStorageClient;

    @Value("${recommendation.weight.likes:1.0}")
    private double wLikes;

    @Value("${recommendation.weight.comments:0.5}")
    private double wComments;

    @Value("${recommendation.weight.recency:100.0}")
    private double wRecency;

    public PageResponse<PostResponse> getNewsfeed(int page, int size, OffsetDateTime anchor) {
        int clampedSize = Math.min(size, 50);
        Long viewerAccountId = resolveViewerAccountId();
        OffsetDateTime effectiveAnchor = anchor != null ? anchor : OffsetDateTime.now();
        List<PostRanked> ranked = postRepository.findRanked(viewerAccountId, wLikes, wComments, wRecency, page, clampedSize, effectiveAnchor);
        long total = postRepository.countActive(effectiveAnchor);
        List<PostResponse> content = ranked.stream().map(this::toResponse).toList();
        return PageResponse.ofAnchored(content, page, clampedSize, total, effectiveAnchor.toString());
    }

    public PageResponse<PostResponse> searchPosts(String keyword, int page, int size, OffsetDateTime anchor) {
        int clampedSize = Math.min(size, 50);
        Long viewerAccountId = resolveViewerAccountId();
        OffsetDateTime effectiveAnchor = anchor != null ? anchor : OffsetDateTime.now();
        List<PostRanked> results = postRepository.searchByKeyword(keyword, viewerAccountId, page, clampedSize, effectiveAnchor);
        long total = postRepository.countByKeyword(keyword, effectiveAnchor);
        List<PostResponse> content = results.stream().map(this::toResponse).toList();
        return PageResponse.ofAnchored(content, page, clampedSize, total, effectiveAnchor.toString());
    }

    public PostResponse getPost(UUID postId) {
        Long viewerAccountId = resolveViewerAccountId();
        PostRanked pr = postRepository.findRankedPostById(postId, viewerAccountId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return toResponse(pr);
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
                .category(request.getCategory())
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
                .category(saved.getCategory())
                .shopAccountId(saved.getAuthor().getId())
                .shopName(saved.getAuthor().getFullName())
                .shopAvatarUrl(saved.getAuthor().getAvatarUrl())
                .createdAt(saved.getCreatedAt())
                .isFollowingShop(false)
                .build();
    }

    public PageResponse<PostResponse> getMyPosts(int page, int size) {
        Account account = requireCurrentAccount();
        int clampedSize = Math.min(size, 50);
        List<PostRanked> posts = postRepository.findByAuthorId(account.getId(), account.getId(), page, clampedSize);
        long total = postRepository.countByAuthorId(account.getId());
        List<PostResponse> content = posts.stream().map(this::toResponse).toList();
        return PageResponse.ofAnchored(content, page, clampedSize, total, null);
    }

    public PageResponse<PostResponse> getShopPosts(Long shopId, int page, int size) {
        int clampedSize = Math.min(size, 50);
        Long viewerAccountId = resolveViewerAccountId();
        List<PostRanked> posts = postRepository.findByAuthorId(shopId, viewerAccountId, page, clampedSize);
        long total = postRepository.countByAuthorId(shopId);
        List<PostResponse> content = posts.stream().map(this::toResponse).toList();
        return PageResponse.ofAnchored(content, page, clampedSize, total, null);
    }

    public PageResponse<PostResponse> getSavedPosts(int page, int size) {
        Account account = requireCurrentAccount();
        int clampedSize = Math.min(size, 50);
        List<PostRanked> posts = postRepository.findSavedByCustomerId(account.getId(), account.getId(), page, clampedSize);
        long total = postRepository.countSavedByCustomerId(account.getId());
        List<PostResponse> content = posts.stream().map(this::toResponse).toList();
        return PageResponse.ofAnchored(content, page, clampedSize, total, null);
    }

    @Transactional
    public PostResponse updatePost(UUID postId, UpdatePostRequest request) {
        Account account = requireCurrentAccount();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Bài viết không tồn tại"));
        if (!post.getAuthor().getId().equals(account.getId())) {
            throw new AccessDeniedException("Không có quyền chỉnh sửa bài viết này");
        }
        postRepository.updatePost(postId, account.getId(), request);
        PostRanked updated = postRepository.findRankedPostById(postId, account.getId())
                .orElseThrow(() -> new RuntimeException("Không thể tải bài viết sau khi cập nhật"));
        return toResponse(updated);
    }

    @Transactional
    public PostResponse updateRemainingQuantity(UUID postId, UpdateRemainingQuantityRequest request) {
        Account account = requireCurrentAccount();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Bài viết không tồn tại"));
        if (!post.getAuthor().getId().equals(account.getId())) {
            throw new AccessDeniedException("Không có quyền chỉnh sửa bài viết này");
        }
        postRepository.updateRemainingQuantity(postId, account.getId(), request.getRemainingQuantity());
        PostRanked updated = postRepository.findRankedPostById(postId, account.getId())
                .orElseThrow(() -> new RuntimeException("Không thể tải bài viết sau khi cập nhật"));
        return toResponse(updated);
    }

    @Transactional
    public void deletePost(UUID postId) {
        Account account = requireCurrentAccount();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Bài viết không tồn tại"));
        if (!post.getAuthor().getId().equals(account.getId())) {
            throw new AccessDeniedException("Không có quyền xóa bài viết này");
        }
        postRepository.deletePost(postId, account.getId());
    }

    @Transactional
    public PostResponse updatePostStatus(UUID postId, UpdatePostStatusRequest request) {
        Account account = requireCurrentAccount();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Bài viết không tồn tại"));
        if (!post.getAuthor().getId().equals(account.getId())) {
            throw new AccessDeniedException("Không có quyền cập nhật trạng thái bài viết này");
        }
        PostStatus newStatus;
        try {
            newStatus = PostStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ: " + request.getStatus());
        }
        if (newStatus == PostStatus.SOLD_OUT || newStatus == PostStatus.EXPIRED) {
            throw new IllegalArgumentException("Không thể đặt trạng thái này thủ công");
        }
        postRepository.updatePostStatus(postId, account.getId(), newStatus);
        PostRanked updated = postRepository.findRankedPostById(postId, account.getId())
                .orElseThrow(() -> new RuntimeException("Không thể tải bài viết sau khi cập nhật"));
        return toResponse(updated);
    }

    public PostImageUploadResponse uploadPostImage(MultipartFile file) {
        Account account = requireCurrentAccount();
        String imageUrl = postImageStorageClient.uploadPostImage(account.getId(), file);
        return new PostImageUploadResponse(imageUrl);
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

    public SavedPostResponse getSavedState(UUID postId) {
        Account account = requireCurrentAccount();
        ensurePostExists(postId);
        return SavedPostResponse.builder()
                .postId(postId)
                .isSaved(postRepository.isSaved(postId, account.getId()))
                .build();
    }

    public SavedPostResponse savePost(UUID postId) {
        Account account = requireCurrentAccount();
        ensurePostExists(postId);
        postRepository.savePost(postId, account.getId());
        return SavedPostResponse.builder()
                .postId(postId)
                .isSaved(true)
                .build();
    }

    public SavedPostResponse unsavePost(UUID postId) {
        Account account = requireCurrentAccount();
        ensurePostExists(postId);
        postRepository.unsavePost(postId, account.getId());
        return SavedPostResponse.builder()
                .postId(postId)
                .isSaved(false)
                .build();
    }

    public PageResponse<CommentResponse> getComments(UUID postId, String cursor, int size) {
        ensurePostExists(postId);
        int clampedSize = Math.min(size, 50);
        OffsetDateTime parsedCursor = null;
        if (cursor != null && !cursor.isBlank()) {
            parsedCursor = OffsetDateTime.parse(cursor);
        }
        // Fetch one extra to determine hasNext
        List<PostComment> raw = postRepository.findComments(postId, parsedCursor, clampedSize + 1);
        boolean hasNext = raw.size() > clampedSize;
        List<PostComment> page = hasNext ? raw.subList(0, clampedSize) : raw;
        List<CommentResponse> comments = page.stream().map(this::toCommentResponse).toList();
        String nextCursor = hasNext ? page.get(page.size() - 1).createdAt().toString() : null;
        return PageResponse.ofCursor(comments, clampedSize, hasNext, nextCursor);
    }

    public CommentResponse createComment(UUID postId, CreateCommentRequest request) {
        Account account = requireCurrentAccount();
        ensurePostExists(postId);
        String content = request.getContent() != null ? request.getContent().trim() : "";
        if (content.isBlank()) {
            throw new IllegalArgumentException("Content must not be blank");
        }
        return toCommentResponse(postRepository.saveComment(postId, account.getId(), content, request.getParentId()));
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
                .category(pr.category())
                .shopAccountId(pr.shopAccountId())
                .shopName(pr.shopName())
                .shopAvatarUrl(pr.shopAvatarUrl())
                .shopAddress(pr.shopAddress())
                .createdAt(pr.createdAt())
                .likeCount(pr.likeCount())
                .isLiked(pr.liked())
                .commentCount(pr.commentCount())
                .isFollowingShop(pr.followingShop())
                .isSaved(pr.saved())
                .build();
    }

    private CommentResponse toCommentResponse(PostComment comment) {
        return CommentResponse.builder()
                .commentId(comment.commentId())
                .authorName(comment.authorName())
                .authorAvatarUrl(comment.authorAvatarUrl())
                .content(comment.content())
                .createdAt(comment.createdAt())
                .parentId(comment.parentId())
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
        if (!postRepository.existsById(postId)) {
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
