package com.normocontrol.infrastructure.persistence.adapter;

import com.normocontrol.domain.model.CheckResult;
import com.normocontrol.domain.port.CheckResultRepository;
import com.normocontrol.infrastructure.persistence.entity.CheckResultEntity;
import com.normocontrol.infrastructure.persistence.mapper.CheckResultMapper;
import com.normocontrol.infrastructure.persistence.repository.SpringDataCheckResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CheckResultRepositoryAdapter implements CheckResultRepository {

    private final SpringDataCheckResultRepository repository;
    private final CheckResultMapper mapper;

    @Override
    public CheckResult save(CheckResult checkResult) {
        CheckResultEntity entity = mapper.toEntity(checkResult);
        CheckResultEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<CheckResult> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<CheckResult> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}
