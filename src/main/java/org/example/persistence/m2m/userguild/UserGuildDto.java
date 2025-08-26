package org.example.persistence.m2m.userguild;

import lombok.Data;
import org.example.persistence.guild.GuildDto;
import org.example.persistence.user.UserDto;

@Data
public class UserGuildDto {
    private UserGuildId id;
    private UserDto user;
    private GuildDto guild;
}
