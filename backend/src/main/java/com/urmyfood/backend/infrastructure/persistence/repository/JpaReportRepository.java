package com.urmyfood.backend.infrastructure.persistence.repository;

import com.urmyfood.backend.domain.model.ReportStatus;
import com.urmyfood.backend.infrastructure.persistence.entity.ReportEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaReportRepository extends JpaRepository<ReportEntity, UUID> {
    Page<ReportEntity> findByStatus(ReportStatus status, Pageable pageable);
}
