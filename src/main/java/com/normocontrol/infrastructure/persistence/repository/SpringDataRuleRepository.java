package com.normocontrol.infrastructure.persistence.repository;

import com.normocontrol.infrastructure.persistence.entity.RuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SpringDataRuleRepository extends JpaRepository<RuleEntity, UUID> {
    
    @Modifying(clearAutomatically = true)
    @Query("UPDATE RuleEntity r SET r.isActive = :active")
    void updateAllStatus(@Param("active") boolean active);
}
