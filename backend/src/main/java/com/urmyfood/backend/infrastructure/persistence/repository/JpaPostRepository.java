package com.urmyfood.backend.infrastructure.persistence.repository;

import com.urmyfood.backend.infrastructure.persistence.entity.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaPostRepository extends JpaRepository<PostEntity, UUID> {
    List<PostEntity> findAllByOrderByCreatedAtDesc();
}
