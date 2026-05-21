package com.urmyfood.backend.domain.repository;

import com.urmyfood.backend.domain.model.Voucher;
import java.util.List;

public interface VoucherRepository {
    List<Voucher> findAllActive();
}
