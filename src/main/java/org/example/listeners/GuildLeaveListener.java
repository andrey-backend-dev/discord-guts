package org.example.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.example.persistence.guild.GuildDataCleanupService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GuildLeaveListener extends ListenerAdapter {

    private final GuildDataCleanupService guildDataCleanupService;

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        var guild = event.getGuild();
        log.info("Bot removed from guild '{}' ({}). Cleaning up persisted data.", guild.getName(), guild.getId());
        guildDataCleanupService.removeGuildData(guild.getIdLong());
    }
}
