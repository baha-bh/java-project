package com.normocontrol.domain.port;

import com.normocontrol.domain.model.Rule;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RuleRepository {
    Rule save(Rule rule);
    Optional<Rule> findById(UUID id);
    List<Rule> findAll();
    void deleteById(UUID id);
}
