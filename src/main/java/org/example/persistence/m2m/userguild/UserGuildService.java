package org.example.persistence.m2m.userguild;

import org.example.persistence.base.BaseService;
import org.example.persistence.guild.GuildDto;
import org.example.persistence.guild.GuildMapper;
import org.example.persistence.m2m.ManyToManyService;
import org.example.persistence.user.UserDto;
import org.example.persistence.user.UserMapper;
import org.springframework.stereotype.Service;

@Service
public class UserGuildService extends BaseService<UserGuildId, UserGuild, UserGuildDto, UserGuildRepository, UserGuildMapper>
        implements ManyToManyService<UserGuildDto, GuildDto, UserDto> {

    private final UserMapper userMapper;
    private final GuildMapper guildMapper;

    protected UserGuildService(
            UserGuildRepository repository, UserGuildMapper mapper, UserMapper userMapper, GuildMapper guildMapper
    ) {
        super(repository, mapper);
        this.userMapper = userMapper;
        this.guildMapper = guildMapper;
    }

    @Override
    public UserGuildDto createRelation(GuildDto guildDto, UserDto userDto) {
        return save(
                UserGuild.builder()
                        .id(new UserGuildId(userDto.getId(), guildDto.getId()))
                        .user(userMapper.dtoToEntity(userDto))
                        .guild(guildMapper.dtoToEntity(guildDto))
                        .build()
        );
    }

}
