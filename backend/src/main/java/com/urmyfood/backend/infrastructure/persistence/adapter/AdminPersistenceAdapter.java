package com.urmyfood.backend.infrastructure.persistence.adapter;

import com.urmyfood.backend.domain.model.Admin;
import com.urmyfood.backend.domain.repository.AdminRepository;
import com.urmyfood.backend.infrastructure.persistence.entity.AdminEntity;
import com.urmyfood.backend.infrastructure.persistence.repository.JpaAdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AdminPersistenceAdapter implements AdminRepository {

    private final JpaAdminRepository jpaAdminRepository;

    @Override
    public Optional<Admin> findByAccountId(Long accountId) {
        return jpaAdminRepository.findByAccountId(accountId)
                .map(this::mapToDomainEntity);
    }

    @Override
    public Admin save(Admin admin) {
        AdminEntity entity = mapToJpaEntity(admin);
        AdminEntity savedEntity = jpaAdminRepository.save(entity);
        return mapToDomainEntity(savedEntity);
    }

    private Admin mapToDomainEntity(AdminEntity entity) {
        return Admin.builder()
                .id(entity.getId())
                .accountId(entity.getAccountId())
                .position(entity.getPosition())
                .shortBio(entity.getShortBio())
                .is2FaEnabled(entity.is2FaEnabled())
                .build();
    }

    private AdminEntity mapToJpaEntity(Admin admin) {
        return AdminEntity.builder()
                .id(admin.getId())
                .accountId(admin.getAccountId())
                .position(admin.getPosition())
                .shortBio(admin.getShortBio())
                .is2FaEnabled(admin.is2FaEnabled())
                .build();
    }
}
