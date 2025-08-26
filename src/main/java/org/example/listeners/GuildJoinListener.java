package org.example.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.example.persistence.guild.GuildService;
import org.example.persistence.m2m.userguild.UserGuildService;
import org.example.persistence.m2m.userguildrole.UserGuildRoleService;
import org.example.persistence.role.GuildRoleDto;
import org.example.persistence.role.GuildRoleService;
import org.example.persistence.user.UserDto;
import org.example.persistence.user.UserService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class GuildJoinListener extends ListenerAdapter {

    private final UserService userService;
    private final GuildService guildService;
    private final GuildRoleService guildRoleService;
    private final UserGuildService userGuildService;
    private final UserGuildRoleService userGuildRoleService;

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        var jdaGuild = event.getGuild();
        saveGuildData(jdaGuild);
    }

    private void saveGuildData(Guild jdaGuild) {
        log.info("Bot is added to guild: {}. Transferring the data to the database.", jdaGuild.getName());

        var guild = guildService.save(jdaGuild);
        var roles = guildRoleService.saveAllRoles(jdaGuild.getRoles());
        var users = userService.saveAllMembers(jdaGuild.getMembers());

        userGuildService.createRelation(Map.of(guild, users));
        Map<UserDto, List<GuildRoleDto>> userRoleRelations = userGuildRoleService.makeUserRoleRelationMap(
                jdaGuild.getMembers(), users, roles
        );
        userGuildRoleService.createRelation(userRoleRelations);

        log.info("Transferring the data of the '{}' guild to the database is ended.", jdaGuild.getName());
    }


}
