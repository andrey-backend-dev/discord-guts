package org.example.persistence.user;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import org.example.persistence.base.BaseService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService extends BaseService<Long, User, UserDto, UserRepository, UserMapper> {

    public UserService(UserRepository repository, UserMapper mapper) {
        super(repository, mapper);
    }

    public List<UserDto> saveAllMembers(List<Member> members) {
        List<User> usersToSave = members.stream().map(getMapper()::jdaMemberToUser).collect(Collectors.toList());
        List<Long> duplicateDiscordIds = getRepository()
                .findByIdIn(usersToSave.stream().map(User::getId).toList())
                .stream().map(User::getId).toList();
        usersToSave.removeIf(user -> duplicateDiscordIds.contains(user.getId()));
        return saveAll(usersToSave);
    }

}
