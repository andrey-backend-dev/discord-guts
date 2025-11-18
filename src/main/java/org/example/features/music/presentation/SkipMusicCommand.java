package org.example.features.music.presentation;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.example.features.music.application.MusicService;
import org.example.features.music.domain.TrackMetadata;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SkipMusicCommand extends AbstractMusicCommand {

    public SkipMusicCommand(MusicService musicService) {
        super(musicService);
    }

    @Override
    public String getName() {
        return "skip";
    }

    @Override
    public String getDescription() {
        return "Пропустить текущий трек";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        var voiceContextOpt = requireVoiceContext(event, true);
        if (voiceContextOpt.isEmpty()) {
            return;
        }
        long guildId = voiceContextOpt.get().guild().getIdLong();
        var skipped = musicService.skip(guildId);
        if (skipped.isEmpty()) {
            event.reply("Сейчас ничего не играет, пропускать нечего.")
                    .setEphemeral(true)
                    .queue(queueSuccessConsumer(log), queueFailureConsumer(log));
            return;
        }
        var track = skipped.get();
        event.reply(String.format("Трек **%s** пропущен.", title(track)))
                .queue(queueSuccessConsumer(log), queueFailureConsumer(log));
    }

    private String title(AudioTrack track) {
        Object data = track.getUserData();
        if (data instanceof TrackMetadata metadata) {
            return metadata.titleOr(track.getInfo().title);
        }
        return track.getInfo().title;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(getName(), getDescription());
    }
}
