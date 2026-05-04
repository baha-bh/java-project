package com.normocontrol.infrastructure.persistence.mapper;

import com.normocontrol.domain.model.Rule;
import com.normocontrol.infrastructure.persistence.entity.RuleEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RuleMapper {
    @org.mapstruct.Mapping(target = "scriptLogic", source = "scriptLogic")
    @org.mapstruct.Mapping(target = "isActive", source = "isActive")
    Rule toDomain(RuleEntity entity);

    @org.mapstruct.Mapping(target = "scriptLogic", source = "scriptLogic")
    @org.mapstruct.Mapping(target = "isActive", source = "isActive")
    RuleEntity toEntity(Rule domain);
}
