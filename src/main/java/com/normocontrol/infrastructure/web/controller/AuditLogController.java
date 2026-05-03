package com.normocontrol.infrastructure.web.controller;

import com.normocontrol.domain.service.AuditLogService;
import com.normocontrol.infrastructure.web.dto.response.AuditLogResponse;
import com.normocontrol.infrastructure.web.mapper.WebAuditLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;
    private final WebAuditLogMapper mapper;

    @GetMapping
    public ResponseEntity<List<AuditLogResponse>> getAllAuditLogs() {
        List<AuditLogResponse> logs = auditLogService.getAllAuditLogs().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(logs);
    }
}
