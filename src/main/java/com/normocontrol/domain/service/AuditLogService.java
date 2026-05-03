package com.normocontrol.domain.service;

import com.normocontrol.domain.model.AuditLog;
import com.normocontrol.domain.port.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLog createAuditLog(AuditLog auditLog) {
        return auditLogRepository.save(auditLog);
    }

    public Optional<AuditLog> getAuditLogById(UUID id) {
        return auditLogRepository.findById(id);
    }

    public List<AuditLog> getAllAuditLogs() {
        return auditLogRepository.findAll();
    }

    public void deleteAuditLog(UUID id) {
        auditLogRepository.deleteById(id);
    }
}
