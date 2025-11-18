package org.example.features.music.presentation;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.example.features.music.application.MusicService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LeaveMusicCommand extends AbstractMusicCommand {

    public LeaveMusicCommand(MusicService musicService) {
        super(musicService);
    }

    @Override
    public String getName() {
        return "leave";
    }

    @Override
    public String getDescription() {
        return "Выйти из голосового канала";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        var voiceContextOpt = requireVoiceContext(event, false);
        if (voiceContextOpt.isEmpty()) {
            return;
        }
        var voiceContext = voiceContextOpt.get();
        long guildId = voiceContext.guild().getIdLong();
        var audioManager = voiceContext.audioManager();
        if (!audioManager.isConnected()) {
            event.reply("Я и так не подключён к голосовому каналу.")
                    .setEphemeral(true)
                    .queue(queueSuccessConsumer(log), queueFailureConsumer(log));
            return;
        }
        audioManager.closeAudioConnection();
        musicService.leaveGuild(guildId);
        event.reply("Отключился от голосового канала и очистил очередь.")
                .queue(queueSuccessConsumer(log), queueFailureConsumer(log));
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(getName(), getDescription());
    }
}
