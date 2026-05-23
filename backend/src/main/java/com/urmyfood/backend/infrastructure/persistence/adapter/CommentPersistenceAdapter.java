package com.urmyfood.backend.infrastructure.persistence.adapter;

import com.urmyfood.backend.domain.model.Comment;
import com.urmyfood.backend.domain.repository.CommentRepository;
import com.urmyfood.backend.infrastructure.persistence.entity.AccountEntity;
import com.urmyfood.backend.infrastructure.persistence.entity.CommentEntity;
import com.urmyfood.backend.infrastructure.persistence.entity.PostEntity;
import com.urmyfood.backend.infrastructure.persistence.repository.JpaAccountRepository;
import com.urmyfood.backend.infrastructure.persistence.repository.JpaCommentRepository;
import com.urmyfood.backend.infrastructure.persistence.repository.JpaPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CommentPersistenceAdapter implements CommentRepository {

    private final JpaCommentRepository jpaCommentRepository;
    private final JpaPostRepository jpaPostRepository;
    private final JpaAccountRepository jpaAccountRepository;
    private final PostPersistenceAdapter postAdapter;
    private final AccountPersistenceAdapter accountAdapter;

    @Override
    public Comment save(Comment comment) {
        PostEntity postEntity = jpaPostRepository.findById(comment.getPost().getPostId())
                .orElseThrow(() -> new RuntimeException("Post not found"));
        AccountEntity accountEntity = jpaAccountRepository.findById(comment.getAccount().getId())
                .orElseThrow(() -> new RuntimeException("Account not found"));
        CommentEntity entity = CommentEntity.builder()
                .post(postEntity)
                .account(accountEntity)
                .content(comment.getContent())
                .build();
        return toDomain(jpaCommentRepository.save(entity));
    }

    @Override
    public List<Comment> findByPostId(UUID postId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return jpaCommentRepository.findByPost_PostId(postId, pageRequest)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public long countByPostId(UUID postId) {
        return jpaCommentRepository.countByPost_PostId(postId);
    }

    private Comment toDomain(CommentEntity entity) {
        return Comment.builder()
                .commentId(entity.getCommentId())
                .post(postAdapter.toDomain(entity.getPost()))
                .account(accountAdapter.toDomain(entity.getAccount()))
                .content(entity.getContent())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
