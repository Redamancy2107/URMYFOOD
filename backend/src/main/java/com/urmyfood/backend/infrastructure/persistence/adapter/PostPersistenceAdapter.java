package com.urmyfood.backend.infrastructure.persistence.adapter;

import com.urmyfood.backend.domain.model.Post;
import com.urmyfood.backend.domain.model.PostComment;
import com.urmyfood.backend.domain.model.PostRanked;
import com.urmyfood.backend.domain.model.PostStatus;
import com.urmyfood.backend.domain.repository.PostRepository;
import com.urmyfood.backend.infrastructure.persistence.entity.AccountEntity;
import com.urmyfood.backend.infrastructure.persistence.entity.PostEntity;
import com.urmyfood.backend.infrastructure.persistence.repository.JpaAccountRepository;
import com.urmyfood.backend.infrastructure.persistence.repository.JpaPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PostPersistenceAdapter implements PostRepository {

    private static final String SELECT_FRAGMENT = """
            SELECT p.post_id, p.dish_name, p.price, p.original_price, p.max_quantity,
                   p.remaining_quantity, p.end_time, p.is_flash_sale, p.status::text,
                   p.content, p.image_url, p.created_at,
                   a.full_name AS shop_name, a.avatar_url AS shop_avatar_url,
                   COALESCE(COUNT(DISTINCT l.like_id), 0) AS like_count,
                   COALESCE(COUNT(DISTINCT c.comment_id), 0) AS comment_count,
                   CASE WHEN :viewerAccountId IS NOT NULL
                        THEN (SELECT COUNT(*) > 0 FROM likes vl
                              WHERE vl.post_id = p.post_id AND vl.account_id = :viewerAccountId)
                        ELSE FALSE END AS is_liked
            FROM posts p
            JOIN accounts a ON a.id = p.author_id
            LEFT JOIN likes l ON l.post_id = p.post_id
            LEFT JOIN comments c ON c.post_id = p.post_id
            """;

    private final JpaPostRepository jpaPostRepository;
    private final JpaAccountRepository jpaAccountRepository;
    private final AccountPersistenceAdapter accountAdapter;
    private final NamedParameterJdbcTemplate jdbc;

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

    @Override
    public List<PostRanked> findRanked(Long viewerAccountId, double w1, double w2, double w3, int page, int size) {
        String sql = SELECT_FRAGMENT + """
                WHERE p.status = 'ACTIVE'
                GROUP BY p.post_id, a.full_name, a.avatar_url
                ORDER BY (
                    :w1 * COALESCE(COUNT(DISTINCT l.like_id), 0) +
                    :w2 * COALESCE(COUNT(DISTINCT c.comment_id), 0) +
                    :w3 * (1.0 / (EXTRACT(EPOCH FROM (NOW() - p.created_at)) / 3600.0 + 1.0))
                ) DESC
                LIMIT :size OFFSET :offset
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("viewerAccountId", viewerAccountId, Types.BIGINT)
                .addValue("w1", w1)
                .addValue("w2", w2)
                .addValue("w3", w3)
                .addValue("size", size)
                .addValue("offset", (long) page * size);

        return jdbc.query(sql, params, this::mapRow);
    }

    @Override
    public long countActive() {
        String sql = "SELECT COUNT(*) FROM posts WHERE status = 'ACTIVE'";
        Long count = jdbc.queryForObject(sql, new MapSqlParameterSource(), Long.class);
        return count != null ? count : 0L;
    }

    @Override
    public List<PostRanked> searchByKeyword(String keyword, Long viewerAccountId, int page, int size) {
        String sql = SELECT_FRAGMENT + """
                WHERE p.status = 'ACTIVE'
                    AND (unaccent(lower(p.dish_name)) LIKE '%' || unaccent(lower(:keyword)) || '%'
                         OR unaccent(lower(a.full_name)) LIKE '%' || unaccent(lower(:keyword)) || '%')
                GROUP BY p.post_id, a.full_name, a.avatar_url
                ORDER BY p.created_at DESC
                LIMIT :size OFFSET :offset
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("viewerAccountId", viewerAccountId, Types.BIGINT)
                .addValue("keyword", keyword)
                .addValue("size", size)
                .addValue("offset", (long) page * size);

        return jdbc.query(sql, params, this::mapRow);
    }

    @Override
    public long countByKeyword(String keyword) {
        String sql = """
                SELECT COUNT(DISTINCT p.post_id)
                FROM posts p
                JOIN accounts a ON a.id = p.author_id
                WHERE p.status = 'ACTIVE'
                    AND (unaccent(lower(p.dish_name)) LIKE '%' || unaccent(lower(:keyword)) || '%'
                         OR unaccent(lower(a.full_name)) LIKE '%' || unaccent(lower(:keyword)) || '%')
                """;
        Long count = jdbc.queryForObject(sql, new MapSqlParameterSource("keyword", keyword), Long.class);
        return count != null ? count : 0L;
    }

    @Override
    public long likePost(UUID postId, Long accountId) {
        String sql = """
                INSERT INTO likes (post_id, account_id)
                VALUES (:postId, :accountId)
                ON CONFLICT (post_id, account_id) DO NOTHING
                """;
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("postId", postId)
                .addValue("accountId", accountId));
        return countLikes(postId);
    }

    @Override
    public long unlikePost(UUID postId, Long accountId) {
        String sql = """
                DELETE FROM likes
                WHERE post_id = :postId AND account_id = :accountId
                """;
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("postId", postId)
                .addValue("accountId", accountId));
        return countLikes(postId);
    }

    @Override
    public List<PostComment> findComments(UUID postId, int page, int size) {
        String sql = """
                SELECT c.comment_id, a.full_name AS author_name, a.avatar_url AS author_avatar_url,
                       c.content, c.created_at
                FROM comments c
                JOIN accounts a ON a.id = c.account_id
                WHERE c.post_id = :postId
                ORDER BY c.created_at DESC
                LIMIT :size OFFSET :offset
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("postId", postId)
                .addValue("size", size)
                .addValue("offset", (long) page * size);
        return jdbc.query(sql, params, this::mapCommentRow);
    }

    @Override
    public long countComments(UUID postId) {
        String sql = "SELECT COUNT(*) FROM comments WHERE post_id = :postId";
        Long count = jdbc.queryForObject(sql, new MapSqlParameterSource("postId", postId), Long.class);
        return count != null ? count : 0L;
    }

    @Override
    public PostComment saveComment(UUID postId, Long accountId, String content) {
        String sql = """
                INSERT INTO comments (post_id, account_id, content)
                VALUES (:postId, :accountId, :content)
                RETURNING comment_id,
                          (SELECT full_name FROM accounts WHERE id = :accountId) AS author_name,
                          (SELECT avatar_url FROM accounts WHERE id = :accountId) AS author_avatar_url,
                          content,
                          created_at
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("postId", postId)
                .addValue("accountId", accountId)
                .addValue("content", content);
        return jdbc.queryForObject(sql, params, this::mapCommentRow);
    }

    private long countLikes(UUID postId) {
        String sql = "SELECT COUNT(*) FROM likes WHERE post_id = :postId";
        Long count = jdbc.queryForObject(sql, new MapSqlParameterSource("postId", postId), Long.class);
        return count != null ? count : 0L;
    }

    private PostRanked mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new PostRanked(
                rs.getObject("post_id", UUID.class),
                rs.getString("dish_name"),
                rs.getBigDecimal("price"),
                rs.getBigDecimal("original_price"),
                rs.getInt("max_quantity"),
                rs.getInt("remaining_quantity"),
                rs.getObject("end_time", OffsetDateTime.class),
                rs.getBoolean("is_flash_sale"),
                PostStatus.valueOf(rs.getString("status")),
                rs.getString("content"),
                rs.getString("image_url"),
                rs.getString("shop_name"),
                rs.getString("shop_avatar_url"),
                rs.getLong("like_count"),
                rs.getLong("comment_count"),
                rs.getBoolean("is_liked"),
                rs.getObject("created_at", OffsetDateTime.class)
        );
    }

    private PostComment mapCommentRow(ResultSet rs, int rowNum) throws SQLException {
        return new PostComment(
                rs.getObject("comment_id", UUID.class),
                rs.getString("author_name"),
                rs.getString("author_avatar_url"),
                rs.getString("content"),
                rs.getObject("created_at", OffsetDateTime.class)
        );
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
