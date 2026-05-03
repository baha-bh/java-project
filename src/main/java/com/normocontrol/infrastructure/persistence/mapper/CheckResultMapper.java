package com.normocontrol.infrastructure.persistence.mapper;

import com.normocontrol.domain.model.CheckResult;
import com.normocontrol.infrastructure.persistence.entity.CheckResultEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {ProjectMapper.class})
public interface CheckResultMapper {
    CheckResult toDomain(CheckResultEntity entity);
    CheckResultEntity toEntity(CheckResult domain);
}
