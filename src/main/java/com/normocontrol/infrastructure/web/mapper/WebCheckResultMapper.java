package com.normocontrol.infrastructure.web.mapper;

import com.normocontrol.domain.model.CheckResult;
import com.normocontrol.infrastructure.web.dto.response.CheckResultResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WebCheckResultMapper {
    @Mapping(source = "project.id", target = "projectId")
    CheckResultResponse toResponse(CheckResult domain);
}
