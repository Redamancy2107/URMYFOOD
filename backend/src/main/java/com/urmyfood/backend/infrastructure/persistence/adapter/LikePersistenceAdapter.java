package com.urmyfood.backend.infrastructure.persistence.adapter;

import com.urmyfood.backend.domain.model.Like;
import com.urmyfood.backend.domain.repository.LikeRepository;
import com.urmyfood.backend.infrastructure.persistence.entity.AccountEntity;
import com.urmyfood.backend.infrastructure.persistence.entity.LikeEntity;
import com.urmyfood.backend.infrastructure.persistence.entity.PostEntity;
import com.urmyfood.backend.infrastructure.persistence.repository.JpaAccountRepository;
import com.urmyfood.backend.infrastructure.persistence.repository.JpaLikeRepository;
import com.urmyfood.backend.infrastructure.persistence.repository.JpaPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LikePersistenceAdapter implements LikeRepository {

    private final JpaLikeRepository jpaLikeRepository;
    private final JpaPostRepository jpaPostRepository;
    private final JpaAccountRepository jpaAccountRepository;

    @Override
    public void save(Like like) {
        PostEntity postEntity = jpaPostRepository.findById(like.getPost().getPostId())
                .orElseThrow(() -> new RuntimeException("Post not found"));
        AccountEntity accountEntity = jpaAccountRepository.findById(like.getAccount().getId())
                .orElseThrow(() -> new RuntimeException("Account not found"));
        LikeEntity entity = LikeEntity.builder()
                .post(postEntity)
                .account(accountEntity)
                .build();
        jpaLikeRepository.save(entity);
    }

    @Override
    public void delete(UUID postId, Long accountId) {
        jpaLikeRepository.deleteByPost_PostIdAndAccount_Id(postId, accountId);
    }

    @Override
    public boolean existsByPostIdAndAccountId(UUID postId, Long accountId) {
        return jpaLikeRepository.existsByPost_PostIdAndAccount_Id(postId, accountId);
    }

    @Override
    public long countByPostId(UUID postId) {
        return jpaLikeRepository.countByPost_PostId(postId);
    }
}
