package com.normocontrol.infrastructure.persistence.adapter;

import com.normocontrol.domain.model.Rule;
import com.normocontrol.domain.port.RuleRepository;
import com.normocontrol.infrastructure.persistence.entity.RuleEntity;
import com.normocontrol.infrastructure.persistence.mapper.RuleMapper;
import com.normocontrol.infrastructure.persistence.repository.SpringDataRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class RuleRepositoryAdapter implements RuleRepository {

    private final SpringDataRuleRepository repository;
    private final RuleMapper mapper;

    @Override
    public Rule save(Rule rule) {
        log.info("Persistence Adapter: Saving rule '{}', script present: {}", rule.getName(), rule.getScriptLogic() != null);
        RuleEntity entity = mapper.toEntity(rule);
        // Manually ensure scriptLogic is copied (failsafe for MapStruct issues)
        entity.setScriptLogic(rule.getScriptLogic());
        
        log.info("Persistence Entity: script_logic present: {}", entity.getScriptLogic() != null);
        RuleEntity saved = repository.save(entity);
        Rule domain = mapper.toDomain(saved);
        domain.setScriptLogic(saved.getScriptLogic()); // Failsafe
        return domain;
    }

    @Override
    public Optional<Rule> findById(UUID id) {
        return repository.findById(id).map(entity -> {
            Rule domain = mapper.toDomain(entity);
            domain.setScriptLogic(entity.getScriptLogic()); // Failsafe
            return domain;
        });
    }

    @Override
    public List<Rule> findAll() {
        return repository.findAll().stream()
                .map(entity -> {
                    Rule domain = mapper.toDomain(entity);
                    domain.setScriptLogic(entity.getScriptLogic()); // Failsafe
                    return domain;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public void updateAllStatus(boolean active) {
        repository.updateAllStatus(active);
    }
}
