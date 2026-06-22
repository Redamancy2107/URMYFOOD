package com.urmyfood.backend.infrastructure.persistence.repository;

import com.urmyfood.backend.infrastructure.persistence.entity.VoucherEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface JpaVoucherRepository extends JpaRepository<VoucherEntity, Long> {
    List<VoucherEntity> findByIsActiveTrueAndExpiryDateAfterOrderByExpiryDateAsc(LocalDate date);
    Optional<VoucherEntity> findByCode(String code);
}

