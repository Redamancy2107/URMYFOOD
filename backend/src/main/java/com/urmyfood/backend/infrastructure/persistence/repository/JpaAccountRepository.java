package com.urmyfood.backend.infrastructure.persistence.repository;

import com.urmyfood.backend.infrastructure.persistence.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaAccountRepository extends JpaRepository<AccountEntity, Long> {
    Optional<AccountEntity> findByEmail(String email);
    Optional<AccountEntity> findByPhone(String phone);
}
