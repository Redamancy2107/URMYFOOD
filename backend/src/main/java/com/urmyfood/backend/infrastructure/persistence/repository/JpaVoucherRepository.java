package com.urmyfood.backend.infrastructure.persistence.repository;

import com.urmyfood.backend.infrastructure.persistence.entity.VoucherEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface JpaVoucherRepository extends JpaRepository<VoucherEntity, Long> {
    List<VoucherEntity> findByIsActiveTrueAndExpiryDateAfterOrderByExpiryDateAsc(LocalDate date);
}
