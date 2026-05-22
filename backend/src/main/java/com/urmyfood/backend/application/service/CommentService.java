package com.urmyfood.backend.application.service;

import com.urmyfood.backend.application.dto.CommentResponse;
import com.urmyfood.backend.application.dto.CreateCommentRequest;
import com.urmyfood.backend.application.dto.PageResponse;
import com.urmyfood.backend.domain.model.Account;
import com.urmyfood.backend.domain.model.Comment;
import com.urmyfood.backend.domain.model.Post;
import com.urmyfood.backend.domain.repository.AccountRepository;
import com.urmyfood.backend.domain.repository.CommentRepository;
import com.urmyfood.backend.domain.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final AccountRepository accountRepository;

    @Transactional(readOnly = true)
    public PageResponse<CommentResponse> getComments(UUID postId, int page, int size) {
        postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        List<CommentResponse> content = commentRepository.findByPostId(postId, page, size)
                .stream()
                .map(this::toResponse)
                .toList();
        long total = commentRepository.countByPostId(postId);
        return PageResponse.of(content, page, size, total);
    }

    public CommentResponse addComment(UUID postId, CreateCommentRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Not authenticated");
        }
        String email = auth.getName();
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Comment comment = Comment.builder()
                .post(post)
                .account(account)
                .content(request.getContent())
                .build();
        return toResponse(commentRepository.save(comment));
    }

    private CommentResponse toResponse(Comment comment) {
        return CommentResponse.builder()
                .commentId(comment.getCommentId())
                .authorName(comment.getAccount().getFullName())
                .authorAvatarUrl(comment.getAccount().getAvatarUrl())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
