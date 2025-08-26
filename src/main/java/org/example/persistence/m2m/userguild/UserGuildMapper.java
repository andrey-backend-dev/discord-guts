package org.example.persistence.m2m.userguild;

import org.example.persistence.base.BaseMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserGuildMapper extends BaseMapper<UserGuild, UserGuildDto> {
}
