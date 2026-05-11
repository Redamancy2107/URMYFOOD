package com.urmyfood.backend.infrastructure.persistence.adapter;

import com.urmyfood.backend.domain.model.Post;
import com.urmyfood.backend.domain.repository.PostRepository;
import com.urmyfood.backend.infrastructure.persistence.entity.AccountEntity;
import com.urmyfood.backend.infrastructure.persistence.entity.PostEntity;
import com.urmyfood.backend.infrastructure.persistence.repository.JpaAccountRepository;
import com.urmyfood.backend.infrastructure.persistence.repository.JpaPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PostPersistenceAdapter implements PostRepository {

    private final JpaPostRepository jpaPostRepository;
    private final JpaAccountRepository jpaAccountRepository;
    private final AccountPersistenceAdapter accountAdapter;

    @Override
    public Post save(Post post) {
        PostEntity entity = toEntity(post);
        return toDomain(jpaPostRepository.save(entity));
    }

    @Override
    public Optional<Post> findById(UUID postId) {
        return jpaPostRepository.findById(postId).map(this::toDomain);
    }

    @Override
    public List<Post> findAllOrderedByCreatedAt() {
        return jpaPostRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    Post toDomain(PostEntity entity) {
        return Post.builder()
                .postId(entity.getPostId())
                .dishName(entity.getDishName())
                .price(entity.getPrice())
                .originalPrice(entity.getOriginalPrice())
                .maxQuantity(entity.getMaxQuantity())
                .remainingQuantity(entity.getRemainingQuantity())
                .endTime(entity.getEndTime())
                .isFlashSale(entity.isFlashSale())
                .status(entity.getStatus())
                .content(entity.getContent())
                .imageUrl(entity.getImageUrl())
                .author(accountAdapter.toDomain(entity.getAuthor()))
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private PostEntity toEntity(Post post) {
        AccountEntity authorEntity = jpaAccountRepository.findById(post.getAuthor().getId())
                .orElseThrow(() -> new RuntimeException("Author account not found: " + post.getAuthor().getId()));
        return PostEntity.builder()
                .postId(post.getPostId())
                .dishName(post.getDishName())
                .price(post.getPrice())
                .originalPrice(post.getOriginalPrice())
                .maxQuantity(post.getMaxQuantity())
                .remainingQuantity(post.getRemainingQuantity())
                .endTime(post.getEndTime())
                .flashSale(post.isFlashSale())
                .status(post.getStatus())
                .content(post.getContent())
                .imageUrl(post.getImageUrl())
                .author(authorEntity)
                .createdAt(post.getCreatedAt())
                .build();
    }
}
