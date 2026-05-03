package com.normocontrol.domain.port;

import com.normocontrol.domain.model.AuditLog;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuditLogRepository {
    AuditLog save(AuditLog auditLog);
    Optional<AuditLog> findById(UUID id);
    List<AuditLog> findAll();
    void deleteById(UUID id);
}
