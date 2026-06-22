package com.urmyfood.backend.domain.repository;

import com.urmyfood.backend.domain.model.Voucher;
import java.util.List;
import java.util.Optional;

public interface VoucherRepository {
    List<Voucher> findAllActive();
    List<Voucher> findSavedByCustomerId(Long customerId);
    Optional<Voucher> findById(Long id);
    Optional<Voucher> findByCode(String code);
    boolean isSaved(Long voucherId, Long customerId);
    void saveVoucher(Long voucherId, Long customerId);
    void unsaveVoucher(Long voucherId, Long customerId);
    Voucher save(Voucher voucher);
    void deleteById(Long id);
    List<Voucher> findAll();
}

