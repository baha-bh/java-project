package com.normocontrol.infrastructure.persistence.adapter;

import com.normocontrol.domain.model.Project;
import com.normocontrol.domain.port.ProjectRepository;
import com.normocontrol.infrastructure.persistence.entity.ProjectEntity;
import com.normocontrol.infrastructure.persistence.mapper.ProjectMapper;
import com.normocontrol.infrastructure.persistence.repository.SpringDataProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProjectRepositoryAdapter implements ProjectRepository {

    private final SpringDataProjectRepository repository;
    private final ProjectMapper mapper;

    @Override
    public Project save(Project project) {
        ProjectEntity entity = mapper.toEntity(project);
        ProjectEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Project> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Project> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}
