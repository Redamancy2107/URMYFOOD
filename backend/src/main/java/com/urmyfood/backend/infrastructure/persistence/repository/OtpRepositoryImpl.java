package com.urmyfood.backend.infrastructure.persistence.repository;

import com.urmyfood.backend.domain.model.Otp;
import com.urmyfood.backend.domain.repository.OtpRepository;
import com.urmyfood.backend.infrastructure.persistence.entity.OtpEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OtpRepositoryImpl implements OtpRepository {

    private final JpaOtpRepository jpaOtpRepository;

    @Override
    public Otp save(Otp otp) {
        OtpEntity entity = OtpEntity.builder()
                .id(otp.getId())
                .email(otp.getEmail())
                .code(otp.getCode())
                .expiryTime(otp.getExpiryTime())
                .used(otp.isUsed())
                .build();
        OtpEntity saved = jpaOtpRepository.save(entity);
        return mapToDomain(saved);
    }

    @Override
    public Optional<Otp> findLatestByEmail(String email) {
        return jpaOtpRepository.findFirstByEmailOrderByCreatedAtDesc(email)
                .map(this::mapToDomain);
    }

    @Override
    public void deleteByEmail(String email) {
        jpaOtpRepository.deleteByEmail(email);
    }

    private Otp mapToDomain(OtpEntity entity) {
        Otp otp = Otp.builder()
                .email(entity.getEmail())
                .code(entity.getCode())
                .expiryTime(entity.getExpiryTime())
                .used(entity.isUsed())
                .build();
        otp.setId(entity.getId());
        otp.setCreatedAt(entity.getCreatedAt());
        otp.setUpdatedAt(entity.getUpdatedAt());
        return otp;
    }
}
