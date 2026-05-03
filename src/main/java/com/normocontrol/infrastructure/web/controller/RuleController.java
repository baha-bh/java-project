package com.normocontrol.infrastructure.web.controller;

import com.normocontrol.domain.model.Rule;
import com.normocontrol.domain.service.RuleService;
import com.normocontrol.infrastructure.web.dto.request.RuleRequest;
import com.normocontrol.infrastructure.web.dto.response.RuleResponse;
import com.normocontrol.infrastructure.web.mapper.WebRuleMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/rules")
@RequiredArgsConstructor
public class RuleController {

    private final RuleService ruleService;
    private final WebRuleMapper mapper;

    @PostMapping
    public ResponseEntity<RuleResponse> createRule(@Valid @RequestBody RuleRequest request) {
        Rule rule = mapper.toDomain(request);
        if (rule.getIsActive() == null) {
            rule.setIsActive(true); // Default true
        }
        Rule created = ruleService.createRule(rule);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(created));
    }

    @GetMapping
    public ResponseEntity<List<RuleResponse>> getAllRules() {
        List<RuleResponse> rules = ruleService.getAllRules().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(rules);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RuleResponse> getRuleById(@PathVariable UUID id) {
        return ruleService.getRuleById(id)
                .map(mapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable UUID id) {
        ruleService.deleteRule(id);
        return ResponseEntity.noContent().build();
    }
}
