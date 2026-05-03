package com.normocontrol.infrastructure.persistence.mapper;

import com.normocontrol.domain.model.Project;
import com.normocontrol.infrastructure.persistence.entity.ProjectEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface ProjectMapper {
    Project toDomain(ProjectEntity entity);
    ProjectEntity toEntity(Project domain);
}
