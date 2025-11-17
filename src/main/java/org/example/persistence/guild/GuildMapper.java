package org.example.persistence.guild;

import org.example.persistence.base.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GuildMapper extends BaseMapper<Guild, GuildDto> {
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "features", ignore = true)
    Guild jdaGuildToGuild(net.dv8tion.jda.api.entities.Guild guild);
}
