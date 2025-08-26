package org.example.persistence.user;

import net.dv8tion.jda.api.entities.Member;
import org.example.persistence.base.BaseMapper;
import org.example.persistence.role.GuildRoleMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = GuildRoleMapper.class)
public interface UserMapper extends BaseMapper<User, UserDto> {
    @Mapping(target = "username", source = "member.user.name")
    User jdaMemberToUser(Member member);
}
