package com.urmyfood.backend.application.service;

import com.urmyfood.backend.application.dto.AddressDto;
import com.urmyfood.backend.application.dto.CreateAddressRequest;
import com.urmyfood.backend.domain.model.Address;
import com.urmyfood.backend.domain.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;

    public List<AddressDto> getAddressesByAccountId(Long accountId) {
        return addressRepository.findByAccountId(accountId)
                .stream().map(this::toDto).toList();
    }

    @Transactional
    public AddressDto createAddress(Long accountId, CreateAddressRequest request) {
        if (request.isDefault()) {
            addressRepository.clearDefaultByAccountId(accountId);
        }

        // Nếu chưa có địa chỉ nào thì tự động đặt làm mặc định
        boolean hasAddresses = !addressRepository.findByAccountId(accountId).isEmpty();

        Address address = Address.builder()
                .accountId(accountId)
                .label(request.getLabel())
                .name(request.getName())
                .phone(request.getPhone())
                .detail(request.getDetail())
                .isDefault(!hasAddresses || request.isDefault())
                .build();

        return toDto(addressRepository.save(address));
    }

    @Transactional
    public AddressDto updateAddress(Long accountId, Long addressId, CreateAddressRequest request) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy địa chỉ"));

        if (!address.getAccountId().equals(accountId)) {
            throw new IllegalArgumentException("Bạn không có quyền sửa địa chỉ này");
        }

        if (request.isDefault()) {
            addressRepository.clearDefaultByAccountId(accountId);
        }

        address.setLabel(request.getLabel());
        address.setName(request.getName());
        address.setPhone(request.getPhone());
        address.setDetail(request.getDetail());
        address.setDefault(request.isDefault());

        return toDto(addressRepository.save(address));
    }

    @Transactional
    public void deleteAddress(Long accountId, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy địa chỉ"));

        if (!address.getAccountId().equals(accountId)) {
            throw new IllegalArgumentException("Bạn không có quyền xóa địa chỉ này");
        }

        boolean wasDefault = address.isDefault();
        addressRepository.deleteById(addressId);

        // Nếu xóa địa chỉ mặc định, đặt địa chỉ đầu tiên còn lại làm mặc định
        if (wasDefault) {
            List<Address> remaining = addressRepository.findByAccountId(accountId);
            if (!remaining.isEmpty()) {
                Address first = remaining.get(0);
                first.setDefault(true);
                addressRepository.save(first);
            }
        }
    }

    @Transactional
    public AddressDto setDefault(Long accountId, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy địa chỉ"));

        if (!address.getAccountId().equals(accountId)) {
            throw new IllegalArgumentException("Bạn không có quyền thay đổi địa chỉ này");
        }

        addressRepository.clearDefaultByAccountId(accountId);
        address.setDefault(true);
        return toDto(addressRepository.save(address));
    }

    private AddressDto toDto(Address a) {
        return AddressDto.builder()
                .id(a.getId())
                .label(a.getLabel())
                .name(a.getName())
                .phone(a.getPhone())
                .detail(a.getDetail())
                .isDefault(a.isDefault())
                .build();
    }
}
