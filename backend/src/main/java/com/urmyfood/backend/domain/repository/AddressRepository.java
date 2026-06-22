package com.urmyfood.backend.domain.repository;

import com.urmyfood.backend.domain.model.Address;
import java.util.List;
import java.util.Optional;

public interface AddressRepository {
    List<Address> findByAccountId(Long accountId);
    Optional<Address> findById(Long id);
    Address save(Address address);
    void deleteById(Long id);
    void clearDefaultByAccountId(Long accountId);
}
