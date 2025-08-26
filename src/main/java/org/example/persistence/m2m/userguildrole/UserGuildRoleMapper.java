package org.example.persistence.m2m.userguildrole;

import org.example.persistence.base.BaseMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserGuildRoleMapper extends BaseMapper<UserGuildRole, UserGuildRoleDto> {
}
