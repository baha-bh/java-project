package com.normocontrol.infrastructure.persistence.adapter;

import com.normocontrol.domain.model.Rule;
import com.normocontrol.domain.port.RuleRepository;
import com.normocontrol.infrastructure.persistence.entity.RuleEntity;
import com.normocontrol.infrastructure.persistence.mapper.RuleMapper;
import com.normocontrol.infrastructure.persistence.repository.SpringDataRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RuleRepositoryAdapter implements RuleRepository {

    private final SpringDataRuleRepository repository;
    private final RuleMapper mapper;

    @Override
    public Rule save(Rule rule) {
        RuleEntity entity = mapper.toEntity(rule);
        RuleEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Rule> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Rule> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}
