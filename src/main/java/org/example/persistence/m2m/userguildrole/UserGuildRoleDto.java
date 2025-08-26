package org.example.persistence.m2m.userguildrole;

import lombok.Data;
import org.example.persistence.role.GuildRoleDto;
import org.example.persistence.user.UserDto;

@Data
public class UserGuildRoleDto {
    private UserGuildRoleId id;
    private UserDto user;
    private GuildRoleDto role;
}

