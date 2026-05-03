package com.normocontrol.domain.service;

import com.normocontrol.domain.model.Rule;
import com.normocontrol.domain.port.RuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RuleService {

    private final RuleRepository ruleRepository;

    public Rule createRule(Rule rule) {
        return ruleRepository.save(rule);
    }

    public Optional<Rule> getRuleById(UUID id) {
        return ruleRepository.findById(id);
    }

    public List<Rule> getAllRules() {
        return ruleRepository.findAll();
    }

    public void deleteRule(UUID id) {
        ruleRepository.deleteById(id);
    }
}
