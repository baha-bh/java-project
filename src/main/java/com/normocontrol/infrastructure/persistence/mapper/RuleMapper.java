package com.normocontrol.infrastructure.persistence.mapper;

import com.normocontrol.domain.model.Rule;
import com.normocontrol.infrastructure.persistence.entity.RuleEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RuleMapper {
    Rule toDomain(RuleEntity entity);
    RuleEntity toEntity(Rule domain);
}
