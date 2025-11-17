package org.example.persistence.m2m.userguild;

import org.springframework.data.repository.CrudRepository;

public interface UserGuildRepository extends CrudRepository<UserGuild, UserGuildId> {

    void deleteAllByGuild_Id(Long guildId);
}
