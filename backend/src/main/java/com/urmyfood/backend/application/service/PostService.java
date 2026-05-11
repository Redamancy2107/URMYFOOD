package com.urmyfood.backend.application.service;

import com.urmyfood.backend.application.dto.CreatePostRequest;
import com.urmyfood.backend.application.dto.PostResponse;
import com.urmyfood.backend.domain.model.Account;
import com.urmyfood.backend.domain.model.Post;
import com.urmyfood.backend.domain.model.PostStatus;
import com.urmyfood.backend.domain.repository.AccountRepository;
import com.urmyfood.backend.domain.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final AccountRepository accountRepository;

    public List<PostResponse> getNewsfeed() {
        return postRepository.findAllOrderedByCreatedAt()
                .stream()
                .map(this::toResponse)
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

        return toResponse(postRepository.save(post));
    }

    private PostResponse toResponse(Post post) {
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
                .build();
    }
}
