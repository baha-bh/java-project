package com.normocontrol.infrastructure.persistence.repository;

import com.normocontrol.infrastructure.persistence.entity.RuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataRuleRepository extends JpaRepository<RuleEntity, UUID> {
}
