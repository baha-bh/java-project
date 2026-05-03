package com.normocontrol.infrastructure.web.controller;

import com.normocontrol.domain.service.ViolationService;
import com.normocontrol.infrastructure.web.dto.response.ViolationResponse;
import com.normocontrol.infrastructure.web.mapper.WebViolationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/violations")
@RequiredArgsConstructor
public class ViolationController {

    private final ViolationService violationService;
    private final WebViolationMapper mapper;

    @GetMapping
    public ResponseEntity<List<ViolationResponse>> getAllViolations() {
        List<ViolationResponse> violations = violationService.getAllViolations().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(violations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ViolationResponse> getViolationById(@PathVariable UUID id) {
        return violationService.getViolationById(id)
                .map(mapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
