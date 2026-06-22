package com.urmyfood.backend.infrastructure.persistence.adapter;

import com.urmyfood.backend.domain.model.AccountActionLog;
import com.urmyfood.backend.domain.repository.AccountActionLogRepository;
import com.urmyfood.backend.infrastructure.persistence.entity.AccountActionLogEntity;
import com.urmyfood.backend.infrastructure.persistence.repository.JpaAccountActionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountActionLogPersistenceAdapter implements AccountActionLogRepository {

    private final JpaAccountActionLogRepository jpaRepository;

    @Override
    public AccountActionLog save(AccountActionLog log) {
        AccountActionLogEntity entity = AccountActionLogEntity.builder()
                .targetType(log.getTargetType())
                .targetIdStr(log.getTargetIdStr())
                .actionType(log.getActionType())
                .reason(log.getReason())
                .build();
        
        AccountActionLogEntity saved = jpaRepository.save(entity);
        
        return AccountActionLog.builder()
                .id(saved.getId())
                .targetType(saved.getTargetType())
                .targetIdStr(saved.getTargetIdStr())
                .actionType(saved.getActionType())
                .reason(saved.getReason())
                .createdAt(saved.getCreatedAt())
                .build();
    }
}
