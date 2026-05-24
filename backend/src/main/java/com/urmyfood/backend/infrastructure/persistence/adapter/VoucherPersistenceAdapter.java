package com.urmyfood.backend.infrastructure.persistence.adapter;

import com.urmyfood.backend.domain.model.Voucher;
import com.urmyfood.backend.domain.repository.VoucherRepository;
import com.urmyfood.backend.infrastructure.persistence.entity.VoucherEntity;
import com.urmyfood.backend.infrastructure.persistence.repository.JpaVoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class VoucherPersistenceAdapter implements VoucherRepository {

    private final JpaVoucherRepository jpaVoucherRepository;

    @Override
    public List<Voucher> findAllActive() {
        return jpaVoucherRepository
                .findByIsActiveTrueAndExpiryDateAfterOrderByExpiryDateAsc(LocalDate.now())
                .stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<Voucher> findById(Long id) {
        return jpaVoucherRepository.findById(id).map(this::toDomain);
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
}
