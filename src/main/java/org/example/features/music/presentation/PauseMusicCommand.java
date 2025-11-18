package org.example.features.music.presentation;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.example.features.music.application.MusicService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PauseMusicCommand extends AbstractMusicCommand {

    public PauseMusicCommand(MusicService musicService) {
        super(musicService);
    }

    @Override
    public String getName() {
        return "pause";
    }

    @Override
    public String getDescription() {
        return "Поставить текущий трек на паузу";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        var voiceContextOpt = requireVoiceContext(event, true);
        if (voiceContextOpt.isEmpty()) {
            return;
        }
        long guildId = voiceContextOpt.get().guild().getIdLong();
        boolean paused = musicService.pause(guildId);
        if (!paused) {
            event.reply("Не удалось поставить на паузу: либо ничего не играет, либо уже стоит на паузе.")
                    .setEphemeral(true)
                    .queue(queueSuccessConsumer(log), queueFailureConsumer(log));
            return;
        }
        event.reply("⏸ Музыка на паузе.")
                .queue(queueSuccessConsumer(log), queueFailureConsumer(log));
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(getName(), getDescription());
    }
}
