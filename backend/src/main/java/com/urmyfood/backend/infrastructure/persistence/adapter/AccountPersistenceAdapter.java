package com.urmyfood.backend.infrastructure.persistence.adapter;

import com.urmyfood.backend.domain.model.Account;
import com.urmyfood.backend.domain.repository.AccountRepository;
import com.urmyfood.backend.infrastructure.persistence.entity.AccountEntity;
import com.urmyfood.backend.infrastructure.persistence.repository.JpaAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AccountPersistenceAdapter implements AccountRepository {

    private final JpaAccountRepository jpaAccountRepository;

    @Override
    public Account save(Account account) {
        AccountEntity entity = toEntity(account);
        AccountEntity savedEntity = jpaAccountRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<Account> findByEmail(String email) {
        return jpaAccountRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public Optional<Account> findByPhone(String phone) {
        return jpaAccountRepository.findByPhone(phone).map(this::toDomain);
    }

    @Override
    public Optional<Account> findById(Long id) {
        return jpaAccountRepository.findById(id).map(this::toDomain);
    }

    private AccountEntity toEntity(Account account) {
        return AccountEntity.builder()
                .id(account.getId())
                .fullName(account.getFullName())
                .email(account.getEmail())
                .phone(account.getPhone())
                .password(account.getPassword())
                .role(account.getRole())
                .build();
    }

    private Account toDomain(AccountEntity entity) {
        return Account.builder()
                .id(entity.getId())
                .fullName(entity.getFullName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .password(entity.getPassword())
                .role(entity.getRole())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
