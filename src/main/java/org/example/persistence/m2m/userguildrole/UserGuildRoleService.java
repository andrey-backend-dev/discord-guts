package org.example.persistence.m2m.userguildrole;

import net.dv8tion.jda.api.entities.Member;
import org.example.persistence.base.BaseService;
import org.example.persistence.m2m.ManyToManyService;
import org.example.persistence.role.GuildRoleDto;
import org.example.persistence.role.GuildRoleMapper;
import org.example.persistence.user.UserDto;
import org.example.persistence.user.UserMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class UserGuildRoleService extends BaseService<UserGuildRoleId, UserGuildRole, UserGuildRoleDto, UserGuildRoleRepository, UserGuildRoleMapper>
        implements ManyToManyService<UserGuildRoleDto, UserDto, GuildRoleDto> {

    private final UserMapper userMapper;
    private final GuildRoleMapper roleMapper;

    protected UserGuildRoleService(
            UserGuildRoleRepository repository, UserGuildRoleMapper mapper, UserMapper userMapper, GuildRoleMapper roleMapper
    ) {
        super(repository, mapper);
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
    }

    @Override
    public UserGuildRoleDto createRelation(UserDto userDto, GuildRoleDto roleDto) {
        return save(
                UserGuildRole.builder()
                        .id(new UserGuildRoleId(userDto.getId(), roleDto.getId()))
                        .user(userMapper.dtoToEntity(userDto))
                        .role(roleMapper.dtoToEntity(roleDto))
                        .build()
        );
    }

    public Map<UserDto, List<GuildRoleDto>> makeUserRoleRelationMap(
            List<Member> members, List<UserDto> users, List<GuildRoleDto> roles
    ) {
        Map<Long, UserDto> userMap = users.stream().collect(Collectors.toMap(UserDto::getId, Function.identity()));
        Map<Long, GuildRoleDto> roleMap = roles.stream().collect(Collectors.toMap(GuildRoleDto::getId, Function.identity()));
        return members.stream()
                .filter(member -> !member.getRoles().isEmpty())
                .collect(Collectors.toMap(
                        member -> userMap.get(member.getIdLong()),
                        member -> member.getRoles().stream().map(role -> roleMap.get(role.getIdLong())).toList())
                );
    }
}
