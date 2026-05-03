package com.normocontrol.infrastructure.persistence.mapper;

import com.normocontrol.domain.model.AuditLog;
import com.normocontrol.infrastructure.persistence.entity.AuditLogEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface AuditLogMapper {
    AuditLog toDomain(AuditLogEntity entity);
    AuditLogEntity toEntity(AuditLog domain);
}
