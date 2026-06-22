package com.urmyfood.backend.infrastructure.persistence.adapter;

import com.urmyfood.backend.domain.model.Address;
import com.urmyfood.backend.domain.repository.AddressRepository;
import com.urmyfood.backend.infrastructure.persistence.entity.AddressEntity;
import com.urmyfood.backend.infrastructure.persistence.repository.JpaAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AddressPersistenceAdapter implements AddressRepository {

    private final JpaAddressRepository jpaAddressRepository;

    @Override
    public List<Address> findByAccountId(Long accountId) {
        return jpaAddressRepository.findByAccountIdOrderByIsDefaultDescCreatedAtDesc(accountId)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<Address> findById(Long id) {
        return jpaAddressRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Address save(Address address) {
        return toDomain(jpaAddressRepository.save(toEntity(address)));
    }

    @Override
    public void deleteById(Long id) {
        jpaAddressRepository.deleteById(id);
    }

    @Override
    public void clearDefaultByAccountId(Long accountId) {
        List<AddressEntity> defaults = jpaAddressRepository.findByAccountIdAndIsDefaultTrue(accountId);
        defaults.forEach(e -> e.setDefault(false));
        jpaAddressRepository.saveAll(defaults);
    }

    private AddressEntity toEntity(Address a) {
        return AddressEntity.builder()
                .id(a.getId())
                .accountId(a.getAccountId())
                .label(a.getLabel())
                .name(a.getName())
                .phone(a.getPhone())
                .detail(a.getDetail())
                .isDefault(a.isDefault())
                .build();
    }

    private Address toDomain(AddressEntity e) {
        return Address.builder()
                .id(e.getId())
                .accountId(e.getAccountId())
                .label(e.getLabel())
                .name(e.getName())
                .phone(e.getPhone())
                .detail(e.getDetail())
                .isDefault(e.isDefault())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
