package com.normocontrol.infrastructure.persistence.repository;

import com.normocontrol.infrastructure.persistence.entity.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataProjectRepository extends JpaRepository<ProjectEntity, UUID> {
}
