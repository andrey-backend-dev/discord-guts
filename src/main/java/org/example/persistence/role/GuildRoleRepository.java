package org.example.persistence.role;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface GuildRoleRepository extends CrudRepository<GuildRole, Long> {

    List<GuildRole> findAllByGuild_IdAndFeatureCrIsChangeableTrue(Long guildId);

    List<GuildRole> findAllByGuild_IdAndFeatureCrIsSettableTrue(Long guildId);

    void deleteAllByGuild_Id(Long guildId);
}
