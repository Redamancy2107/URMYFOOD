package com.urmyfood.backend.application.service;

import com.urmyfood.backend.application.dto.LikeToggleResult;
import com.urmyfood.backend.domain.model.Account;
import com.urmyfood.backend.domain.model.Like;
import com.urmyfood.backend.domain.model.Post;
import com.urmyfood.backend.domain.repository.AccountRepository;
import com.urmyfood.backend.domain.repository.LikeRepository;
import com.urmyfood.backend.domain.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public LikeToggleResult toggleLike(UUID postId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Not authenticated");
        }
        String email = auth.getName();
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        boolean alreadyLiked = likeRepository.existsByPostIdAndAccountId(postId, account.getId());
        if (alreadyLiked) {
            likeRepository.delete(postId, account.getId());
        } else {
            Like like = Like.builder().post(post).account(account).build();
            likeRepository.save(like);
        }

        long likeCount = likeRepository.countByPostId(postId);
        boolean isLiked = !alreadyLiked;
        return LikeToggleResult.builder().likeCount(likeCount).isLiked(isLiked).build();
    }
}
