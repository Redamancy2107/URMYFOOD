package com.urmyfood.backend.infrastructure.persistence.adapter;

import com.urmyfood.backend.domain.model.Voucher;
import com.urmyfood.backend.domain.repository.VoucherRepository;
import com.urmyfood.backend.infrastructure.persistence.entity.VoucherEntity;
import com.urmyfood.backend.infrastructure.persistence.repository.JpaVoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class VoucherPersistenceAdapter implements VoucherRepository {

    private final JpaVoucherRepository jpaVoucherRepository;
    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public List<Voucher> findAllActive() {
        return jpaVoucherRepository
                .findByIsActiveTrueAndExpiryDateGreaterThanEqualOrderByExpiryDateAsc(LocalDate.now())
                .stream().map(this::toDomain).toList();
    }

    @Override
    public List<Voucher> findSavedByCustomerId(Long customerId) {
        String sql = """
                SELECT v.id, v.code, v.title, v.description, v.discount_value, v.min_order_value,
                       v.expiry_date, v.is_active, v.created_at, v.updated_at
                FROM saved_vouchers sv
                JOIN vouchers v ON v.id = sv.voucher_id
                WHERE sv.customer_id = :customerId
                  AND v.is_active = TRUE
                  AND v.expiry_date >= CURRENT_DATE
                ORDER BY sv.created_at DESC
                """;
        return jdbc.query(sql, new MapSqlParameterSource("customerId", customerId), this::mapVoucherRow);
    }

    @Override
    public Optional<Voucher> findById(Long id) {
        return jpaVoucherRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Voucher> findByCode(String code) {
        return jpaVoucherRepository.findByCode(code).map(this::toDomain);
    }

    @Override
    public boolean isSaved(Long voucherId, Long customerId) {
        String sql = """
                SELECT COUNT(*) > 0
                FROM saved_vouchers
                WHERE voucher_id = :voucherId AND customer_id = :customerId
                """;
        Boolean saved = jdbc.queryForObject(sql, new MapSqlParameterSource()
                .addValue("voucherId", voucherId)
                .addValue("customerId", customerId), Boolean.class);
        return Boolean.TRUE.equals(saved);
    }

    @Override
    public void saveVoucher(Long voucherId, Long customerId) {
        String sql = """
                INSERT INTO saved_vouchers (customer_id, voucher_id)
                VALUES (:customerId, :voucherId)
                ON CONFLICT (customer_id, voucher_id) DO NOTHING
                """;
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("customerId", customerId)
                .addValue("voucherId", voucherId));
    }

    @Override
    public void unsaveVoucher(Long voucherId, Long customerId) {
        String sql = """
                DELETE FROM saved_vouchers
                WHERE customer_id = :customerId AND voucher_id = :voucherId
                """;
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("customerId", customerId)
                .addValue("voucherId", voucherId));
    }

    @Override
    public Voucher save(Voucher voucher) {
        return toDomain(jpaVoucherRepository.save(toEntity(voucher)));
    }

    @Override
    public void deleteById(Long id) {
        jpaVoucherRepository.deleteById(id);
    }

    @Override
    public List<Voucher> findAll() {
        return jpaVoucherRepository.findAll().stream().map(this::toDomain).toList();
    }

    VoucherEntity toEntity(Voucher v) {
        return VoucherEntity.builder()
                .id(v.getId())
                .code(v.getCode())
                .title(v.getTitle())
                .description(v.getDescription())
                .discountValue(v.getDiscountValue())
                .minOrderValue(v.getMinOrderValue())
                .expiryDate(v.getExpiryDate())
                .isActive(v.isActive())
                .build();
    }

    Voucher toDomain(VoucherEntity e) {
        return Voucher.builder()
                .id(e.getId())
                .code(e.getCode())
                .title(e.getTitle())
                .description(e.getDescription())
                .discountValue(e.getDiscountValue())
                .minOrderValue(e.getMinOrderValue())
                .expiryDate(e.getExpiryDate())
                .isActive(e.isActive())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    private Voucher mapVoucherRow(ResultSet rs, int rowNum) throws SQLException {
        return Voucher.builder()
                .id(rs.getLong("id"))
                .code(rs.getString("code"))
                .title(rs.getString("title"))
                .description(rs.getString("description"))
                .discountValue(rs.getBigDecimal("discount_value"))
                .minOrderValue(rs.getBigDecimal("min_order_value"))
                .expiryDate(rs.getObject("expiry_date", LocalDate.class))
                .isActive(rs.getBoolean("is_active"))
                .createdAt(toLocalDateTime(rs.getObject("created_at", OffsetDateTime.class)))
                .updatedAt(toLocalDateTime(rs.getObject("updated_at", OffsetDateTime.class)))
                .build();
    }

    private java.time.LocalDateTime toLocalDateTime(OffsetDateTime value) {
        return value != null ? value.toLocalDateTime() : null;
    }
}
