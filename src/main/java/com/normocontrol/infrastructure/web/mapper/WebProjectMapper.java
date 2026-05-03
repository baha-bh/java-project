package com.normocontrol.infrastructure.web.mapper;

import com.normocontrol.domain.model.Project;
import com.normocontrol.infrastructure.web.dto.request.ProjectRequest;
import com.normocontrol.infrastructure.web.dto.response.ProjectResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WebProjectMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Project toDomain(ProjectRequest request);

    @Mapping(source = "owner.id", target = "ownerId")
    ProjectResponse toResponse(Project domain);
}
