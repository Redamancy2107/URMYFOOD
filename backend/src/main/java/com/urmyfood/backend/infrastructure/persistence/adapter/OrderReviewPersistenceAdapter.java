package com.urmyfood.backend.infrastructure.persistence.adapter;

import com.urmyfood.backend.domain.model.OrderReview;
import com.urmyfood.backend.domain.repository.OrderReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderReviewPersistenceAdapter implements OrderReviewRepository {

    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public Optional<OrderReview> findByOrderId(UUID orderId) {
        String sql = baseSelect() + " WHERE order_id = :orderId";
        List<OrderReview> result = jdbc.query(sql, new MapSqlParameterSource("orderId", orderId), this::mapRow);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public Optional<OrderReview> findByOrderIdAndCustomerId(UUID orderId, Long customerId) {
        String sql = baseSelect() + " WHERE order_id = :orderId AND customer_id = :customerId";
        List<OrderReview> result = jdbc.query(sql, new MapSqlParameterSource()
                .addValue("orderId", orderId)
                .addValue("customerId", customerId), this::mapRow);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public boolean existsByOrderId(UUID orderId) {
        String sql = "SELECT COUNT(*) > 0 FROM order_reviews WHERE order_id = :orderId";
        Boolean exists = jdbc.queryForObject(sql, new MapSqlParameterSource("orderId", orderId), Boolean.class);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public OrderReview save(OrderReview review) {
        String sql = """
                INSERT INTO order_reviews (order_id, customer_id, shop_id, rating, comment)
                VALUES (:orderId, :customerId, :shopId, :rating, :comment)
                RETURNING id, order_id, customer_id, shop_id, rating, comment, created_at, updated_at
                """;
        return jdbc.queryForObject(sql, new MapSqlParameterSource()
                .addValue("orderId", review.getOrderId())
                .addValue("customerId", review.getCustomerId())
                .addValue("shopId", review.getShopId())
                .addValue("rating", review.getRating())
                .addValue("comment", review.getComment()), this::mapRow);
    }

    private String baseSelect() {
        return """
                SELECT id, order_id, customer_id, shop_id, rating, comment, created_at, updated_at
                FROM order_reviews
                """;
    }

    private OrderReview mapRow(ResultSet rs, int rowNum) throws SQLException {
        return OrderReview.builder()
                .id(rs.getObject("id", UUID.class))
                .orderId(rs.getObject("order_id", UUID.class))
                .customerId(rs.getLong("customer_id"))
                .shopId(rs.getLong("shop_id"))
                .rating(rs.getInt("rating"))
                .comment(rs.getString("comment"))
                .createdAt(rs.getObject("created_at", OffsetDateTime.class))
                .updatedAt(rs.getObject("updated_at", OffsetDateTime.class))
                .build();
    }
}
