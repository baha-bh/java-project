package com.normocontrol.infrastructure.web.mapper;

import com.normocontrol.domain.model.Rule;
import com.normocontrol.infrastructure.web.dto.request.RuleRequest;
import com.normocontrol.infrastructure.web.dto.response.RuleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WebRuleMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Rule toDomain(RuleRequest request);

    RuleResponse toResponse(Rule domain);
}
