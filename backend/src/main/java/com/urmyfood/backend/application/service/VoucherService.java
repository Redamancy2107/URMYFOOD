package com.urmyfood.backend.application.service;

import com.urmyfood.backend.application.dto.VoucherDto;
import com.urmyfood.backend.domain.model.Voucher;
import com.urmyfood.backend.domain.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VoucherService {

    private final VoucherRepository voucherRepository;

    public List<VoucherDto> getActiveVouchers() {
        return voucherRepository.findAllActive()
                .stream().map(this::toDto).toList();
    }

    private VoucherDto toDto(Voucher v) {
        return VoucherDto.builder()
                .id(v.getId())
                .code(v.getCode())
                .title(v.getTitle())
                .description(v.getDescription())
                .discountValue(v.getDiscountValue())
                .minOrderValue(v.getMinOrderValue())
                .expiryDate(v.getExpiryDate())
                .build();
    }
}
