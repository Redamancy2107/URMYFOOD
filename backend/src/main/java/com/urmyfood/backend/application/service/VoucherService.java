package com.urmyfood.backend.application.service;

import com.urmyfood.backend.application.dto.VoucherDto;
import com.urmyfood.backend.application.dto.SavedVoucherResponse;
import com.urmyfood.backend.domain.model.Voucher;
import com.urmyfood.backend.domain.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VoucherService {

    private final VoucherRepository voucherRepository;

    public List<VoucherDto> getActiveVouchers() {
        return getActiveVouchers(null);
    }

    public List<VoucherDto> getActiveVouchers(Long viewerId) {
        return voucherRepository.findAllActive()
                .stream().map(voucher -> toDto(voucher, viewerId)).toList();
    }

    public List<VoucherDto> getSavedVouchers(Long customerId) {
        return voucherRepository.findSavedByCustomerId(customerId)
                .stream().map(voucher -> toDto(voucher, customerId)).toList();
    }

    public SavedVoucherResponse saveVoucher(Long customerId, Long voucherId) {
        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy voucher"));
        if (!voucher.isActive()) {
            throw new IllegalArgumentException("Voucher không còn hoạt động");
        }
        if (voucher.getExpiryDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Voucher đã hết hạn");
        }
        voucherRepository.saveVoucher(voucherId, customerId);
        return SavedVoucherResponse.builder()
                .voucherId(voucherId)
                .isSaved(true)
                .build();
    }

    public SavedVoucherResponse unsaveVoucher(Long customerId, Long voucherId) {
        voucherRepository.unsaveVoucher(voucherId, customerId);
        return SavedVoucherResponse.builder()
                .voucherId(voucherId)
                .isSaved(false)
                .build();
    }

    private VoucherDto toDto(Voucher v) {
        return toDto(v, null);
    }

    private VoucherDto toDto(Voucher v, Long viewerId) {
        return VoucherDto.builder()
                .id(v.getId())
                .code(v.getCode())
                .title(v.getTitle())
                .description(v.getDescription())
                .discountValue(v.getDiscountValue())
                .minOrderValue(v.getMinOrderValue())
                .expiryDate(v.getExpiryDate())
                .isSaved(viewerId != null && voucherRepository.isSaved(v.getId(), viewerId))
                .build();
    }
}
