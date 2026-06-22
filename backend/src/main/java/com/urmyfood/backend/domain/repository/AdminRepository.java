package com.urmyfood.backend.domain.repository;

import com.urmyfood.backend.domain.model.Admin;
import java.util.Optional;

public interface AdminRepository {
    Optional<Admin> findByAccountId(Long accountId);
    Admin save(Admin admin);
}
