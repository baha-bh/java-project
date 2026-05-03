package com.normocontrol.infrastructure.web.mapper;

import com.normocontrol.domain.model.AuditLog;
import com.normocontrol.infrastructure.web.dto.response.AuditLogResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WebAuditLogMapper {
    @Mapping(source = "user.id", target = "userId")
    AuditLogResponse toResponse(AuditLog domain);
}
