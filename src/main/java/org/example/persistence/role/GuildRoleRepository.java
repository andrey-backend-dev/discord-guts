package org.example.persistence.role;

import org.springframework.data.repository.CrudRepository;

public interface GuildRoleRepository extends CrudRepository<GuildRole, Long> {

    void deleteAllByGuild_Id(Long guildId);
}
