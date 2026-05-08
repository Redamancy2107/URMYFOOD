package com.urmyfood.backend.domain.repository;

import com.urmyfood.backend.domain.model.Account;
import java.util.Optional;

public interface AccountRepository {
    Account save(Account account);
    Optional<Account> findByEmail(String email);
    Optional<Account> findByPhone(String phone);
    Optional<Account> findById(Long id);
}
