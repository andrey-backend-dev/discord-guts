package org.example.persistence.guild;

import org.example.persistence.base.BaseService;
import org.springframework.stereotype.Service;


@Service
public class GuildService extends BaseService<Long, Guild, GuildDto, GuildRepository, GuildMapper> {

    public GuildService(GuildRepository repository, GuildMapper mapper) {
        super(repository, mapper);
    }

    public GuildDto save(net.dv8tion.jda.api.entities.Guild guild) {
        return save(getMapper().jdaGuildToGuild(guild));
    }

}
