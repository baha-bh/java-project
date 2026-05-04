package com.normocontrol.infrastructure.web.controller;

import com.normocontrol.domain.model.Rule;
import com.normocontrol.domain.service.RuleService;
import com.normocontrol.infrastructure.web.dto.request.RuleRequest;
import com.normocontrol.infrastructure.web.dto.response.RuleResponse;
import com.normocontrol.infrastructure.web.mapper.WebRuleMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.normocontrol.infrastructure.ai.GeminiAiService;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/rules")
@RequiredArgsConstructor
@Slf4j
public class RuleController {

    private final RuleService ruleService;
    private final WebRuleMapper mapper;
    private final GeminiAiService aiService;

    @PostMapping("/generate-ai")
    public ResponseEntity<Map<String, String>> generateAiRule(@RequestBody Map<String, String> request) {
        String description = request.get("description");
        if (description == null || description.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Description is required"));
        }
        String script = aiService.generateGroovyScript(description);
        return ResponseEntity.ok(Map.of("script", script));
    }

    @PostMapping("/toggle-all")
    @Transactional
    public ResponseEntity<Void> toggleAllRules(@RequestParam boolean active) {
        log.info("Toggling all rules to active={}", active);
        ruleService.toggleAllStatus(active);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<RuleResponse> createRule(@Valid @RequestBody RuleRequest request) {
        log.info("Creating new rule: name={}, category={}, scriptLength={}", 
            request.getName(), request.getCategory(), 
            request.getScriptLogic() != null ? request.getScriptLogic().length() : "null");
        
        if (request.getScriptLogic() == null || request.getScriptLogic().isBlank()) {
            log.warn("Rule '{}' has NO script logic!", request.getName());
        }

        Rule rule = mapper.toDomain(request);
        // Manually ensure scriptLogic is copied (failsafe for MapStruct issues)
        rule.setScriptLogic(request.getScriptLogic());
        
        log.info("Mapped domain rule: name={}, scriptPresent={}, scriptLength={}", 
            rule.getName(), 
            rule.getScriptLogic() != null,
            rule.getScriptLogic() != null ? rule.getScriptLogic().length() : 0);

        if (rule.getIsActive() == null) {
            rule.setIsActive(true);
        }
        if (rule.getCode() == null || rule.getCode().isBlank()) {
            // Generate a safe code. If name is Russian, the regex might return empty string.
            String safeCode = rule.getName().toUpperCase().replaceAll("\\s+", "_").replaceAll("[^A-Z0-9_]", "");
            if (safeCode.isBlank()) {
                safeCode = "RULE_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            }
            rule.setCode(safeCode);
        }
        if (rule.getCode().length() > 50) {
            rule.setCode(rule.getCode().substring(0, 50));
        }
        Rule created = ruleService.createRule(rule);
        log.info("Rule created successfully: ID={}", created.getId());
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

    @org.springframework.web.bind.annotation.PatchMapping("/{id}/toggle")
    public ResponseEntity<RuleResponse> toggleRule(@PathVariable UUID id) {
        return ruleService.toggleRuleStatus(id)
                .map(mapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
