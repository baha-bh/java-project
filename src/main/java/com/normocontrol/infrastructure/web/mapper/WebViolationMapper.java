package com.normocontrol.infrastructure.web.mapper;

import com.normocontrol.domain.model.Violation;
import com.normocontrol.infrastructure.web.dto.response.ViolationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {WebRuleMapper.class})
public interface WebViolationMapper {
    @Mapping(source = "checkResult.id", target = "checkResultId")
    ViolationResponse toResponse(Violation domain);
}
