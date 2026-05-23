package com.urmyfood.backend.application.service;

import com.urmyfood.backend.application.dto.CreatePostRequest;
import com.urmyfood.backend.application.dto.PostResponse;
import com.urmyfood.backend.domain.model.Account;
import com.urmyfood.backend.domain.model.Post;
import com.urmyfood.backend.domain.model.PostStatus;
import com.urmyfood.backend.domain.repository.AccountRepository;
import com.urmyfood.backend.domain.repository.CommentRepository;
import com.urmyfood.backend.domain.repository.LikeRepository;
import com.urmyfood.backend.domain.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final AccountRepository accountRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;

    public List<PostResponse> getNewsfeed() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<Account> viewer = Optional.empty();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            viewer = accountRepository.findByEmail(auth.getName());
        }
        final Optional<Account> finalViewer = viewer;
        return postRepository.findAllOrderedByCreatedAt()
                .stream()
                .map(post -> toResponse(post, finalViewer))
                .toList();
    }

    public PostResponse createPost(CreatePostRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Not authenticated");
        }
        String email = auth.getName();
        Account author = accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Account not found"));

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

        return toResponse(postRepository.save(post), Optional.empty());
    }

    private PostResponse toResponse(Post post, Optional<Account> viewer) {
        long likeCount = likeRepository.countByPostId(post.getPostId());
        long commentCount = commentRepository.countByPostId(post.getPostId());
        boolean isLiked = viewer
                .map(acc -> likeRepository.existsByPostIdAndAccountId(post.getPostId(), acc.getId()))
                .orElse(false);

        return PostResponse.builder()
                .postId(post.getPostId())
                .dishName(post.getDishName())
                .price(post.getPrice())
                .originalPrice(post.getOriginalPrice())
                .maxQuantity(post.getMaxQuantity())
                .remainingQuantity(post.getRemainingQuantity())
                .endTime(post.getEndTime())
                .isFlashSale(post.isFlashSale())
                .status(post.getStatus().name())
                .content(post.getContent())
                .imageUrl(post.getImageUrl())
                .shopName(post.getAuthor().getFullName())
                .shopAvatarUrl(null)
                .createdAt(post.getCreatedAt())
                .likeCount(likeCount)
                .isLiked(isLiked)
                .commentCount(commentCount)
                .build();
    }
}
