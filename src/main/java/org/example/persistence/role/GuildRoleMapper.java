package org.example.persistence.role;

import net.dv8tion.jda.api.entities.Role;
import org.example.persistence.base.BaseMapper;
import org.example.persistence.guild.GuildMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = GuildMapper.class)
public interface GuildRoleMapper extends BaseMapper<GuildRole, GuildRoleDto> {
    GuildRole jdaRoleToGuildRole(Role role);
}
