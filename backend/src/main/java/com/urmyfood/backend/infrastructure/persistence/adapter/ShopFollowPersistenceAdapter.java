package com.urmyfood.backend.infrastructure.persistence.adapter;

import com.urmyfood.backend.domain.repository.ShopFollowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShopFollowPersistenceAdapter implements ShopFollowRepository {

    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public void follow(Long customerId, Long shopId) {
        String sql = """
                INSERT INTO shop_follows (customer_id, shop_id)
                VALUES (:customerId, :shopId)
                ON CONFLICT (customer_id, shop_id) DO NOTHING
                """;
        jdbc.update(sql, params(customerId, shopId));
    }

    @Override
    public void unfollow(Long customerId, Long shopId) {
        String sql = """
                DELETE FROM shop_follows
                WHERE customer_id = :customerId AND shop_id = :shopId
                """;
        jdbc.update(sql, params(customerId, shopId));
    }

    @Override
    public boolean isFollowing(Long customerId, Long shopId) {
        if (customerId == null || shopId == null) {
            return false;
        }
        String sql = """
                SELECT EXISTS (
                    SELECT 1 FROM shop_follows
                    WHERE customer_id = :customerId AND shop_id = :shopId
                )
                """;
        Boolean exists = jdbc.queryForObject(sql, params(customerId, shopId), Boolean.class);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public long countByShopId(Long shopId) {
        String sql = "SELECT COUNT(*) FROM shop_follows WHERE shop_id = :shopId";
        Long count = jdbc.queryForObject(
                sql,
                new MapSqlParameterSource("shopId", shopId),
                Long.class
        );
        return count == null ? 0L : count;
    }

    private MapSqlParameterSource params(Long customerId, Long shopId) {
        return new MapSqlParameterSource()
                .addValue("customerId", customerId)
                .addValue("shopId", shopId);
    }
}
