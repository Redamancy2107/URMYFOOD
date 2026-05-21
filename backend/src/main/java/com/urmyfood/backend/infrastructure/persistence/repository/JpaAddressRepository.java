package com.urmyfood.backend.infrastructure.persistence.repository;

import com.urmyfood.backend.infrastructure.persistence.entity.AddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaAddressRepository extends JpaRepository<AddressEntity, Long> {
    List<AddressEntity> findByAccountIdOrderByIsDefaultDescCreatedAtDesc(Long accountId);
    List<AddressEntity> findByAccountIdAndIsDefaultTrue(Long accountId);
}
