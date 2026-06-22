package com.urmyfood.backend.domain.repository;

import com.urmyfood.backend.domain.model.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface ReportRepository {
    Report save(Report report);
    Optional<Report> findById(UUID id);
    Page<Report> findAllByStatus(String status, Pageable pageable);
}
