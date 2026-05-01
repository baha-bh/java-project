package com.normocontrol.infrastructure.persistence.mapper;

import com.normocontrol.domain.model.User;
import com.normocontrol.infrastructure.persistence.entity.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toDomain(UserEntity entity);
    UserEntity toEntity(User domain);
}
