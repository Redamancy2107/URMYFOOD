package com.urmyfood.backend.infrastructure.persistence.adapter;

import com.urmyfood.backend.domain.model.Report;
import com.urmyfood.backend.domain.model.ReportStatus;
import com.urmyfood.backend.domain.repository.ReportRepository;
import com.urmyfood.backend.infrastructure.persistence.entity.AccountEntity;
import com.urmyfood.backend.infrastructure.persistence.entity.PostEntity;
import com.urmyfood.backend.infrastructure.persistence.entity.ReportEntity;
import com.urmyfood.backend.infrastructure.persistence.repository.JpaAccountRepository;
import com.urmyfood.backend.infrastructure.persistence.repository.JpaPostRepository;
import com.urmyfood.backend.infrastructure.persistence.repository.JpaReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReportPersistenceAdapter implements ReportRepository {

    private final JpaReportRepository jpaReportRepository;
    private final JpaAccountRepository jpaAccountRepository;
    private final JpaPostRepository jpaPostRepository;
    private final PostPersistenceAdapter postAdapter;
    private final AccountPersistenceAdapter accountAdapter;

    @Override
    public Report save(Report report) {
        ReportEntity entity = toEntity(report);
        return toDomain(jpaReportRepository.save(entity));
    }

    @Override
    public Optional<Report> findById(UUID id) {
        return jpaReportRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Page<Report> findAllByStatus(String status, Pageable pageable) {
        if (status == null || status.isBlank()) {
            return jpaReportRepository.findAll(pageable).map(this::toDomain);
        }
        return jpaReportRepository.findByStatus(ReportStatus.valueOf(status.toUpperCase()), pageable).map(this::toDomain);
    }

    private Report toDomain(ReportEntity entity) {
        if (entity == null) return null;
        return Report.builder()
                .reportId(entity.getReportId())
                .post(postAdapter.toDomain(entity.getPost()))
                .reporter(accountAdapter.toDomain(entity.getReporter()))
                .reason(entity.getReason())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .resolvedAt(entity.getResolvedAt())
                .resolvedBy(entity.getResolvedBy() != null ? accountAdapter.toDomain(entity.getResolvedBy()) : null)
                .build();
    }

    private ReportEntity toEntity(Report report) {
        if (report == null) return null;
        
        PostEntity postEntity = report.getPost() != null 
                ? jpaPostRepository.findById(report.getPost().getPostId()).orElse(null) 
                : null;
                
        AccountEntity reporterEntity = report.getReporter() != null 
                ? jpaAccountRepository.findById(report.getReporter().getId()).orElse(null) 
                : null;
                
        AccountEntity resolvedByEntity = report.getResolvedBy() != null 
                ? jpaAccountRepository.findById(report.getResolvedBy().getId()).orElse(null) 
                : null;

        return ReportEntity.builder()
                .reportId(report.getReportId())
                .post(postEntity)
                .reporter(reporterEntity)
                .reason(report.getReason())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .resolvedAt(report.getResolvedAt())
                .resolvedBy(resolvedByEntity)
                .build();
    }
}
