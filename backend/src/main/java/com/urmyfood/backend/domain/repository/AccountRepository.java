package com.urmyfood.backend.domain.repository;

import com.urmyfood.backend.domain.model.Account;
import java.util.List;
import java.util.Optional;

public interface AccountRepository {
    Account save(Account account);
    Optional<Account> findByEmail(String email);
    Optional<Account> findByPhone(String phone);
    Optional<Account> findById(Long id);
    List<Account> findAll(int page, int size, String role, String sortBy, String sortDir);
    long count(String role);
}
