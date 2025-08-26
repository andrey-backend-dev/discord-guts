package org.example.persistence.role;

import net.dv8tion.jda.api.entities.Role;
import org.example.persistence.base.BaseService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GuildRoleService extends BaseService<Long, GuildRole, GuildRoleDto, GuildRoleRepository, GuildRoleMapper> {

    protected GuildRoleService(GuildRoleRepository repository, GuildRoleMapper mapper) {
        super(repository, mapper);
    }

    public List<GuildRoleDto> saveAllRoles(List<Role> roles) {
        return saveAll(getGuildRolesFromJdaRoles(roles));
    }

    private List<GuildRole> getGuildRolesFromJdaRoles(List<Role> roles) {
        return roles.stream().map(role -> getMapper().jdaRoleToGuildRole(role)).toList();
    }

}
