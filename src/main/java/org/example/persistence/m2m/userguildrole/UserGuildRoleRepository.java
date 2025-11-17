package org.example.persistence.m2m.userguildrole;

import org.springframework.data.repository.CrudRepository;

public interface UserGuildRoleRepository extends CrudRepository<UserGuildRole, UserGuildRoleId> {

    void deleteAllByRole_Guild_Id(Long guildId);
}
