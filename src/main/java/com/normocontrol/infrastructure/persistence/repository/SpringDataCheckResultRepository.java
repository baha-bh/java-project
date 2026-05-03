package com.normocontrol.infrastructure.persistence.repository;

import com.normocontrol.infrastructure.persistence.entity.CheckResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataCheckResultRepository extends JpaRepository<CheckResultEntity, UUID> {
}
