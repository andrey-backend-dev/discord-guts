package org.example.features.music.presentation;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.example.features.music.application.MusicService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ResumeMusicCommand extends AbstractMusicCommand {

    public ResumeMusicCommand(MusicService musicService) {
        super(musicService);
    }

    @Override
    public String getName() {
        return "resume";
    }

    @Override
    public String getDescription() {
        return "Продолжить воспроизведение";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        var voiceContextOpt = requireVoiceContext(event, true);
        if (voiceContextOpt.isEmpty()) {
            return;
        }
        long guildId = voiceContextOpt.get().guild().getIdLong();
        boolean resumed = musicService.resume(guildId);
        if (!resumed) {
            event.reply("Нечего продолжать: либо ничего не играет, либо музыка уже идёт.")
                    .setEphemeral(true)
                    .queue(queueSuccessConsumer(log), queueFailureConsumer(log));
            return;
        }
        event.reply("▶️ Продолжаем воспроизведение.")
                .queue(queueSuccessConsumer(log), queueFailureConsumer(log));
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(getName(), getDescription());
    }
}
