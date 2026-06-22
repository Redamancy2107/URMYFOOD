package com.urmyfood.backend.infrastructure.persistence.adapter;

import com.urmyfood.backend.domain.model.Account;
import com.urmyfood.backend.domain.repository.AccountRepository;
import com.urmyfood.backend.infrastructure.persistence.entity.AccountEntity;
import com.urmyfood.backend.infrastructure.persistence.repository.JpaAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
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
                .avatarUrl(account.getAvatarUrl())
                .isActive(account.isActive())
                .build();
    }

    @Override
    public List<Account> findAll(int page, int size, String role, String sortBy, String sortDir) {
        Sort.Direction direction = "ASC".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortProperty = sortBy != null && !sortBy.trim().isEmpty() ? sortBy : "id";
        if ("createdAt".equals(sortProperty)) {
            sortProperty = "id";
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortProperty));
        if (role == null || role.trim().isEmpty()) {
            return jpaAccountRepository.findAll(pageable).stream().map(this::toDomain).toList();
        }
        return jpaAccountRepository.findByRole(role, pageable).stream().map(this::toDomain).toList();
    }

    @Override
    public long count(String role) {
        if (role == null || role.trim().isEmpty()) {
            return jpaAccountRepository.count();
        }
        return jpaAccountRepository.countByRole(role);
    }

    Account toDomain(AccountEntity entity) {
        return Account.builder()
                .id(entity.getId())
                .fullName(entity.getFullName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .password(entity.getPassword())
                .role(entity.getRole())
                .avatarUrl(entity.getAvatarUrl())
                .isActive(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
