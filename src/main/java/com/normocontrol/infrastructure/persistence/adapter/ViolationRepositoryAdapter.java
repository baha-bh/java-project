package com.normocontrol.infrastructure.persistence.adapter;

import com.normocontrol.domain.model.Violation;
import com.normocontrol.domain.port.ViolationRepository;
import com.normocontrol.infrastructure.persistence.entity.ViolationEntity;
import com.normocontrol.infrastructure.persistence.mapper.ViolationMapper;
import com.normocontrol.infrastructure.persistence.repository.SpringDataViolationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ViolationRepositoryAdapter implements ViolationRepository {

    private final SpringDataViolationRepository repository;
    private final ViolationMapper mapper;

    @Override
    public Violation save(Violation violation) {
        ViolationEntity entity = mapper.toEntity(violation);
        ViolationEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Violation> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Violation> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}
