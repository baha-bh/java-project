package com.normocontrol.infrastructure.persistence.mapper;

import com.normocontrol.domain.model.Violation;
import com.normocontrol.infrastructure.persistence.entity.ViolationEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {CheckResultMapper.class, RuleMapper.class})
public interface ViolationMapper {
    Violation toDomain(ViolationEntity entity);
    ViolationEntity toEntity(Violation domain);
}
