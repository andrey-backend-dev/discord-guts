package org.example.persistence.guild;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.persistence.m2m.guildfeature.GuildFeatureRepository;
import org.example.persistence.m2m.userguild.UserGuildRepository;
import org.example.persistence.m2m.userguildrole.UserGuildRoleRepository;
import org.example.persistence.role.GuildRoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuildDataCleanupService {

    private final UserGuildRoleRepository userGuildRoleRepository;
    private final GuildRoleRepository guildRoleRepository;
    private final UserGuildRepository userGuildRepository;
    private final GuildFeatureRepository guildFeatureRepository;
    private final GuildRepository guildRepository;

    @Transactional
    public void removeGuildData(Long guildId) {
        log.info("Removing persisted data for guild with id {}.", guildId);
        userGuildRoleRepository.deleteAllByRole_Guild_Id(guildId);
        guildRoleRepository.deleteAllByGuild_Id(guildId);
        userGuildRepository.deleteAllByGuild_Id(guildId);
        guildFeatureRepository.deleteAllByGuild_Id(guildId);
        guildRepository.deleteById(guildId);
    }
}
