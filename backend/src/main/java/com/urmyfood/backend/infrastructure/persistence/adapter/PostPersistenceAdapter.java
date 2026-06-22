package com.urmyfood.backend.infrastructure.persistence.adapter;

import com.urmyfood.backend.application.dto.UpdatePostRequest;
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
import java.sql.Timestamp;
import java.sql.Types;
import java.text.Normalizer;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class PostPersistenceAdapter implements PostRepository {

    private static final Set<String> SEARCH_STOP_WORDS = Set.of(
            "toi", "minh", "muon", "an", "mon", "tim", "kiem", "cho",
            "can", "cai", "mot", "va", "la", "co", "gi", "hom", "nay"
    );

    private static final String SEARCH_DOCUMENT = "unaccent(lower(concat_ws(' ', p.dish_name, p.content, a.full_name)))";
    private static final String SEARCH_DISH = "unaccent(lower(p.dish_name))";
    private static final String SEARCH_SHOP = "unaccent(lower(COALESCE(sp.shop_name, a.full_name)))";

    private static final String SELECT_FRAGMENT = """
            SELECT p.post_id, p.dish_name, p.price, p.original_price, p.max_quantity,
                   p.remaining_quantity, p.end_time, p.is_flash_sale, p.status::text,
                   p.content, p.image_url, p.category, p.created_at,
                   a.id AS shop_account_id, COALESCE(sp.shop_name, a.full_name) AS shop_name, COALESCE(sp.logo_url, a.avatar_url) AS shop_avatar_url,
                   sp.address AS shop_address,
                   COALESCE(COUNT(DISTINCT l.like_id), 0) AS like_count,
                   COALESCE(COUNT(DISTINCT c.comment_id), 0) AS comment_count,
                   CASE WHEN :viewerAccountId IS NOT NULL
                        THEN (SELECT COUNT(*) > 0 FROM likes vl
                              WHERE vl.post_id = p.post_id AND vl.account_id = :viewerAccountId)
                        ELSE FALSE END AS is_liked,
                   CASE WHEN :viewerAccountId IS NOT NULL
                        THEN (SELECT COUNT(*) > 0 FROM shop_follows sf
                              WHERE sf.shop_id = a.id AND sf.customer_id = :viewerAccountId)
                        ELSE FALSE END AS is_following_shop,
                   CASE WHEN :viewerAccountId IS NOT NULL
                        THEN (SELECT COUNT(*) > 0 FROM saved_posts sv
                              WHERE sv.post_id = p.post_id AND sv.customer_id = :viewerAccountId)
                        ELSE FALSE END AS is_saved
            FROM posts p
            JOIN accounts a ON a.id = p.author_id
            LEFT JOIN shop_profiles sp ON sp.shop_id = a.id
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
    public Optional<PostRanked> findRankedPostById(UUID postId, Long viewerAccountId) {
        String sql = SELECT_FRAGMENT + """
                WHERE p.post_id = :postId
                GROUP BY p.post_id, a.id, a.full_name, a.avatar_url, sp.shop_name, sp.logo_url, sp.address
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("postId", postId)
                .addValue("viewerAccountId", viewerAccountId, Types.BIGINT);
        List<PostRanked> result = jdbc.query(sql, params, this::mapRow);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public Optional<Post> findById(UUID postId) {
        return jpaPostRepository.findById(postId).map(this::toDomain);
    }

    @Override
    public boolean existsById(UUID postId) {
        return jpaPostRepository.existsById(postId);
    }

    @Override
    public Optional<Post> findByIdForUpdate(UUID postId) {
        return jpaPostRepository.findByIdForUpdate(postId).map(this::toDomain);
    }

    @Override
    public List<Post> findAllOrderedByCreatedAt() {
        return jpaPostRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<PostRanked> findRanked(Long viewerAccountId, double w1, double w2, double w3, int page, int size, OffsetDateTime anchor) {
        String sql = SELECT_FRAGMENT + """
                WHERE p.status = 'ACTIVE' AND p.created_at <= :anchor
                GROUP BY p.post_id, a.id, a.full_name, a.avatar_url, sp.shop_name, sp.logo_url, sp.address
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
                .addValue("anchor", Timestamp.from(anchor.toInstant()), Types.TIMESTAMP)
                .addValue("size", size)
                .addValue("offset", (long) page * size);

        return jdbc.query(sql, params, this::mapRow);
    }

    @Override
    public long countActive(OffsetDateTime anchor) {
        String sql = "SELECT COUNT(*) FROM posts WHERE status = 'ACTIVE' AND created_at <= :anchor";
        Long count = jdbc.queryForObject(sql, new MapSqlParameterSource("anchor", Timestamp.from(anchor.toInstant())), Long.class);
        return count != null ? count : 0L;
    }

    @Override
    public List<PostRanked> searchByKeyword(String keyword, Long viewerAccountId, int page, int size, OffsetDateTime anchor) {
        SearchTerms terms = searchTerms(keyword);
        if (terms.query().isBlank()) {
            return List.of();
        }

        String sql = SELECT_FRAGMENT + """
                WHERE p.status = 'ACTIVE' AND p.created_at <= :anchor
                    AND (
                """ + searchPredicate(terms) + """
                    )
                GROUP BY p.post_id, a.id, a.full_name, a.avatar_url, sp.shop_name, sp.logo_url, sp.address
                ORDER BY
                """ + relevanceExpression(terms) + """
                    DESC,
                    COALESCE(COUNT(DISTINCT l.like_id), 0) DESC,
                    COALESCE(COUNT(DISTINCT c.comment_id), 0) DESC,
                    p.created_at DESC
                LIMIT :size OFFSET :offset
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("viewerAccountId", viewerAccountId, Types.BIGINT)
                .addValue("query", terms.query())
                .addValue("anchor", Timestamp.from(anchor.toInstant()), Types.TIMESTAMP)
                .addValue("size", size)
                .addValue("offset", (long) page * size);
        addSearchParams(params, terms);

        return jdbc.query(sql, params, this::mapRow);
    }

    @Override
    public long countByKeyword(String keyword, OffsetDateTime anchor) {
        SearchTerms terms = searchTerms(keyword);
        if (terms.query().isBlank()) {
            return 0L;
        }

        String sql = """
                SELECT COUNT(DISTINCT p.post_id)
                FROM posts p
                JOIN accounts a ON a.id = p.author_id
                LEFT JOIN shop_profiles sp ON sp.shop_id = a.id
                WHERE p.status = 'ACTIVE' AND p.created_at <= :anchor
                    AND (
                """ + searchPredicate(terms) + """
                    )
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("query", terms.query())
                .addValue("anchor", Timestamp.from(anchor.toInstant()), Types.TIMESTAMP);
        addSearchParams(params, terms);
        Long count = jdbc.queryForObject(sql, params, Long.class);
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
    public List<PostRanked> findSavedByCustomerId(Long customerId, Long viewerAccountId, int page, int size) {
        String sql = SELECT_FRAGMENT + """
                JOIN saved_posts sv ON sv.post_id = p.post_id AND sv.customer_id = :customerId
                WHERE p.status = 'ACTIVE'
                GROUP BY p.post_id, a.id, a.full_name, a.avatar_url, sp.shop_name, sp.logo_url, sp.address, sv.created_at
                ORDER BY sv.created_at DESC
                LIMIT :size OFFSET :offset
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("customerId", customerId)
                .addValue("viewerAccountId", viewerAccountId, Types.BIGINT)
                .addValue("size", size)
                .addValue("offset", (long) page * size);
        return jdbc.query(sql, params, this::mapRow);
    }

    @Override
    public long countSavedByCustomerId(Long customerId) {
        String sql = """
                SELECT COUNT(*)
                FROM saved_posts sv
                JOIN posts p ON p.post_id = sv.post_id
                WHERE sv.customer_id = :customerId AND p.status = 'ACTIVE'
                """;
        Long count = jdbc.queryForObject(sql, new MapSqlParameterSource("customerId", customerId), Long.class);
        return count != null ? count : 0L;
    }

    @Override
    public boolean isSaved(UUID postId, Long customerId) {
        String sql = """
                SELECT COUNT(*) > 0
                FROM saved_posts
                WHERE post_id = :postId AND customer_id = :customerId
                """;
        Boolean saved = jdbc.queryForObject(sql, new MapSqlParameterSource()
                .addValue("postId", postId)
                .addValue("customerId", customerId), Boolean.class);
        return Boolean.TRUE.equals(saved);
    }

    @Override
    public void savePost(UUID postId, Long customerId) {
        String sql = """
                INSERT INTO saved_posts (customer_id, post_id)
                VALUES (:customerId, :postId)
                ON CONFLICT (customer_id, post_id) DO NOTHING
                """;
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("customerId", customerId)
                .addValue("postId", postId));
    }

    @Override
    public void unsavePost(UUID postId, Long customerId) {
        String sql = """
                DELETE FROM saved_posts
                WHERE customer_id = :customerId AND post_id = :postId
                """;
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("customerId", customerId)
                .addValue("postId", postId));
    }

    @Override
    public List<PostComment> findComments(UUID postId, OffsetDateTime cursor, int size) {
        String cursorClause = cursor != null ? "AND c.created_at < :cursor" : "";
        String sql = """
                SELECT c.comment_id, a.full_name AS author_name, a.avatar_url AS author_avatar_url,
                       c.content, c.created_at, c.parent_id
                FROM comments c
                JOIN accounts a ON a.id = c.account_id
                WHERE c.post_id = :postId %s
                ORDER BY c.created_at DESC
                LIMIT :size
                """.formatted(cursorClause);
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("postId", postId)
                .addValue("size", size);
        if (cursor != null) {
            params.addValue("cursor", Timestamp.from(cursor.toInstant()), Types.TIMESTAMP);
        }
        return jdbc.query(sql, params, this::mapCommentRow);
    }

    @Override
    public long countComments(UUID postId) {
        String sql = "SELECT COUNT(*) FROM comments WHERE post_id = :postId";
        Long count = jdbc.queryForObject(sql, new MapSqlParameterSource("postId", postId), Long.class);
        return count != null ? count : 0L;
    }

    @Override
    public PostComment saveComment(UUID postId, Long accountId, String content, UUID parentId) {
        String sql = """
                INSERT INTO comments (post_id, account_id, content, parent_id)
                VALUES (:postId, :accountId, :content, :parentId)
                RETURNING comment_id,
                          (SELECT full_name FROM accounts WHERE id = :accountId) AS author_name,
                          (SELECT avatar_url FROM accounts WHERE id = :accountId) AS author_avatar_url,
                          content,
                          created_at,
                          parent_id
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("postId", postId)
                .addValue("accountId", accountId)
                .addValue("content", content)
                .addValue("parentId", parentId, Types.OTHER);
        return jdbc.queryForObject(sql, params, this::mapCommentRow);
    }

    private long countLikes(UUID postId) {
        String sql = "SELECT COUNT(*) FROM likes WHERE post_id = :postId";
        Long count = jdbc.queryForObject(sql, new MapSqlParameterSource("postId", postId), Long.class);
        return count != null ? count : 0L;
    }

    static SearchTerms searchTerms(String keyword) {
        String normalized = normalizeSearch(keyword);
        List<String> tokens = Arrays.stream(normalized.split(" "))
                .filter(token -> !token.isBlank())
                .filter(token -> !SEARCH_STOP_WORDS.contains(token))
                .distinct()
                .toList();
        String query = tokens.isEmpty() ? normalized : String.join(" ", tokens);
        List<String> regexes = tokens.stream()
                .map(PostPersistenceAdapter::subsequenceRegex)
                .toList();
        return new SearchTerms(query, tokens, regexes);
    }

    private static String normalizeSearch(String keyword) {
        if (keyword == null) {
            return "";
        }
        String withoutMarks = Normalizer.normalize(keyword, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return withoutMarks.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static String subsequenceRegex(String token) {
        return token.chars()
                .mapToObj(ch -> Character.toString((char) ch))
                .collect(Collectors.joining(".*")) + ".*";
    }

    private String searchPredicate(SearchTerms terms) {
        String basePredicate = """
                    %s LIKE '%%' || :query || '%%'
                    OR %s LIKE '%%' || :query || '%%'
                    OR similarity(%s, :query) > 0.12
                    OR similarity(%s, :query) > 0.18
                    OR word_similarity(:query, %s) > 0.35
                """.formatted(SEARCH_DOCUMENT, SEARCH_SHOP, SEARCH_DOCUMENT, SEARCH_DISH, SEARCH_DOCUMENT);
        String tokenPredicate = tokenPredicates(terms, " OR ");
        return tokenPredicate.isBlank() ? basePredicate : basePredicate + " OR " + tokenPredicate;
    }

    private String relevanceExpression(SearchTerms terms) {
        String tokenScore = IntStream.range(0, terms.tokens().size())
                .mapToObj(i -> """
                        CASE
                            WHEN %s LIKE '%%' || :token%d || '%%' THEN 30
                            WHEN length(:token%d) <= 3 AND %s ~ :tokenRegex%d THEN 18
                            WHEN word_similarity(:token%d, %s) > 0.45 THEN 16
                            WHEN similarity(%s, :token%d) > 0.12 THEN 12
                            ELSE 0
                        END
                        """.formatted(SEARCH_DOCUMENT, i, i, SEARCH_DOCUMENT, i, i, SEARCH_DOCUMENT, SEARCH_DOCUMENT, i))
                .collect(Collectors.joining(" + "));
        String tokenPart = tokenScore.isBlank() ? "0" : tokenScore;
        return """
                    (
                        CASE WHEN %s = :query THEN 100 ELSE 0 END +
                        CASE WHEN %s LIKE :query || '%%' THEN 60 ELSE 0 END +
                        CASE WHEN %s LIKE '%%' || :query || '%%' THEN 45 ELSE 0 END +
                        CASE WHEN %s LIKE '%%' || :query || '%%' THEN 25 ELSE 0 END +
                        GREATEST(similarity(%s, :query), word_similarity(:query, %s)) * 40 +
                        %s +
                        COALESCE(COUNT(DISTINCT l.like_id), 0) * 0.6 +
                        COALESCE(COUNT(DISTINCT c.comment_id), 0) * 0.4 +
                        8.0 / (EXTRACT(EPOCH FROM (NOW() - p.created_at)) / 86400.0 + 1.0)
                    )
                """.formatted(SEARCH_DISH, SEARCH_DISH, SEARCH_DOCUMENT, SEARCH_SHOP, SEARCH_DOCUMENT, SEARCH_DOCUMENT, tokenPart);
    }

    private String tokenPredicates(SearchTerms terms, String delimiter) {
        return IntStream.range(0, terms.tokens().size())
                .mapToObj(i -> """
                        %s LIKE '%%' || :token%d || '%%'
                        OR word_similarity(:token%d, %s) > 0.45
                        OR similarity(%s, :token%d) > 0.12
                        OR (length(:token%d) <= 3 AND %s ~ :tokenRegex%d)
                        """.formatted(SEARCH_DOCUMENT, i, i, SEARCH_DOCUMENT, SEARCH_DOCUMENT, i, i, SEARCH_DOCUMENT, i))
                .collect(Collectors.joining(delimiter));
    }

    private void addSearchParams(MapSqlParameterSource params, SearchTerms terms) {
        for (int i = 0; i < terms.tokens().size(); i++) {
            params.addValue("token" + i, terms.tokens().get(i));
            params.addValue("tokenRegex" + i, terms.tokenRegexes().get(i));
        }
    }

    record SearchTerms(String query, List<String> tokens, List<String> tokenRegexes) {
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
                rs.getLong("shop_account_id"),
                rs.getString("shop_name"),
                rs.getString("shop_avatar_url"),
                rs.getString("shop_address"),
                rs.getLong("like_count"),
                rs.getLong("comment_count"),
                rs.getBoolean("is_liked"),
                rs.getBoolean("is_following_shop"),
                rs.getBoolean("is_saved"),
                rs.getObject("created_at", OffsetDateTime.class),
                rs.getString("category")
        );
    }

    private PostComment mapCommentRow(ResultSet rs, int rowNum) throws SQLException {
        return new PostComment(
                rs.getObject("comment_id", UUID.class),
                rs.getString("author_name"),
                rs.getString("author_avatar_url"),
                rs.getString("content"),
                rs.getObject("created_at", OffsetDateTime.class),
                rs.getObject("parent_id", UUID.class)
        );
    }

    @Override
    public List<PostRanked> findByAuthorId(Long authorId, Long viewerAccountId, int page, int size) {
        String sql = SELECT_FRAGMENT + """
                WHERE p.author_id = :authorId
                GROUP BY p.post_id, a.id, a.full_name, a.avatar_url, sp.shop_name, sp.logo_url, sp.address
                ORDER BY p.created_at DESC
                LIMIT :size OFFSET :offset
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("viewerAccountId", viewerAccountId, Types.BIGINT)
                .addValue("authorId", authorId)
                .addValue("size", size)
                .addValue("offset", (long) page * size);
        return jdbc.query(sql, params, this::mapRow);
    }

    @Override
    public long countByAuthorId(Long accountId) {
        String sql = "SELECT COUNT(*) FROM posts WHERE author_id = :authorId";
        Long count = jdbc.queryForObject(sql, new MapSqlParameterSource("authorId", accountId), Long.class);
        return count != null ? count : 0L;
    }

    @Override
    public void updatePost(UUID postId, Long authorId, UpdatePostRequest req) {
        String sql = """
                UPDATE posts SET
                    dish_name = :dishName,
                    price = :price,
                    original_price = :originalPrice,
                    max_quantity = :maxQuantity,
                    remaining_quantity = LEAST(remaining_quantity, :maxQuantity),
                    end_time = :endTime,
                    is_flash_sale = :isFlashSale,
                    content = :content,
                    image_url = :imageUrl,
                    category = :category
                WHERE post_id = :postId AND author_id = :authorId
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("dishName", req.getDishName())
                .addValue("price", req.getPrice())
                .addValue("originalPrice", req.getOriginalPrice())
                .addValue("maxQuantity", req.getMaxQuantity())
                .addValue("endTime", req.getEndTime() != null
                        ? Timestamp.from(req.getEndTime().toInstant()) : null, Types.TIMESTAMP)
                .addValue("isFlashSale", req.isFlashSale())
                .addValue("content", req.getContent())
                .addValue("imageUrl", req.getImageUrl())
                .addValue("category", req.getCategory())
                .addValue("postId", postId)
                .addValue("authorId", authorId);
        jdbc.update(sql, params);
    }

    @Override
    public void updateRemainingQuantity(UUID postId, Long authorId, int remainingQuantity) {
        String sql = """
                UPDATE posts SET
                    remaining_quantity = LEAST(GREATEST(:remaining, 0), max_quantity)
                WHERE post_id = :postId AND author_id = :authorId
                """;
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("remaining", remainingQuantity)
                .addValue("postId", postId)
                .addValue("authorId", authorId));
    }

    @Override
    public void deletePost(UUID postId, Long authorId) {
        String sql = "DELETE FROM posts WHERE post_id = :postId AND author_id = :authorId";
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("postId", postId)
                .addValue("authorId", authorId));
    }

    @Override
    public void updatePostStatus(UUID postId, Long authorId, PostStatus status) {
        String sql = "UPDATE posts SET status = :status WHERE post_id = :postId AND author_id = :authorId";
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("status", status.name())
                .addValue("postId", postId)
                .addValue("authorId", authorId));
    }

    @Override
    public List<Post> findAll(int page, int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by("createdAt").descending());
        return jpaPostRepository.findAll(pageable).stream().map(this::toDomain).toList();
    }

    @Override
    public long countAll() {
        return jpaPostRepository.count();
    }

    @Override
    public void adminUpdatePostStatus(UUID postId, PostStatus status) {
        String sql = "UPDATE posts SET status = :status WHERE post_id = :postId";
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("status", status.name())
                .addValue("postId", postId));
    }

    @Override
    public void adminDeletePost(UUID postId) {
        String sql = "DELETE FROM posts WHERE post_id = :postId";
        jdbc.update(sql, new MapSqlParameterSource().addValue("postId", postId));
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
                .category(entity.getCategory())
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
                .category(post.getCategory())
                .author(authorEntity)
                .createdAt(post.getCreatedAt())
                .build();
    }
}
