package com.urmyfood.backend.domain.repository;

import com.urmyfood.backend.domain.model.AccountActionLog;

public interface AccountActionLogRepository {
    AccountActionLog save(AccountActionLog log);
}
