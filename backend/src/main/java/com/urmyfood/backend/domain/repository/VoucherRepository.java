package com.urmyfood.backend.domain.repository;

import com.urmyfood.backend.domain.model.Voucher;
import java.util.List;
import java.util.Optional;

public interface VoucherRepository {
    List<Voucher> findAllActive();
    Optional<Voucher> findById(Long id);
    Optional<Voucher> findByCode(String code);
}

