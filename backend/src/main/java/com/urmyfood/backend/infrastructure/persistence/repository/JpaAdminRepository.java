package com.urmyfood.backend.infrastructure.persistence.repository;

import com.urmyfood.backend.infrastructure.persistence.entity.AdminEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaAdminRepository extends JpaRepository<AdminEntity, Long> {
    Optional<AdminEntity> findByAccountId(Long accountId);
}
