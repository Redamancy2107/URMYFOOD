package com.urmyfood.backend.infrastructure.persistence.repository;

import com.urmyfood.backend.infrastructure.persistence.entity.OtpEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface JpaOtpRepository extends JpaRepository<OtpEntity, UUID> {
    Optional<OtpEntity> findFirstByEmailOrderByCreatedAtDesc(String email);
    void deleteByEmail(String email);
}
