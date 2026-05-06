package com.urmyfood.backend.domain.repository;

import com.urmyfood.backend.domain.model.Otp;
import java.util.Optional;

public interface OtpRepository {
    Otp save(Otp otp);
    Optional<Otp> findLatestByEmail(String email);
    void deleteByEmail(String email);
}
