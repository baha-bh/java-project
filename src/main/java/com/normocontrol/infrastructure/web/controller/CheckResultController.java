package com.normocontrol.infrastructure.web.controller;

import com.normocontrol.domain.service.CheckResultService;
import com.normocontrol.infrastructure.web.dto.response.CheckResultResponse;
import com.normocontrol.infrastructure.web.mapper.WebCheckResultMapper;
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
@RequestMapping("/api/v1/checks")
@RequiredArgsConstructor
public class CheckResultController {

    private final CheckResultService checkResultService;
    private final WebCheckResultMapper mapper;

    @GetMapping
    public ResponseEntity<List<CheckResultResponse>> getAllCheckResults() {
        List<CheckResultResponse> checks = checkResultService.getAllCheckResults().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(checks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CheckResultResponse> getCheckResultById(@PathVariable UUID id) {
        return checkResultService.getCheckResultById(id)
                .map(mapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
