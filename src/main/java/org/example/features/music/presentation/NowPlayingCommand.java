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
public class NowPlayingCommand extends AbstractMusicCommand {

    public NowPlayingCommand(MusicService musicService) {
        super(musicService);
    }

    @Override
    public String getName() {
        return "nowplaying";
    }

    @Override
    public String getDescription() {
        return "Показать информацию о текущем треке";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        var voiceContextOpt = requireVoiceContext(event, true);
        if (voiceContextOpt.isEmpty()) {
            return;
        }
        long guildId = voiceContextOpt.get().guild().getIdLong();
        var trackOpt = musicService.nowPlaying(guildId);
        if (trackOpt.isEmpty()) {
            event.reply("Сейчас ничего не играет.")
                    .setEphemeral(true)
                    .queue(queueSuccessConsumer(log), queueFailureConsumer(log));
            return;
        }
        AudioTrack track = trackOpt.get();
        String current = formatDuration(track.getPosition());
        String total = formatDuration(track.getDuration());
        String requester = requester(track);
        String message = String.format("Сейчас играет **%s** [`%s/%s`] — запросил %s.",
                track.getInfo().title, current, total, requester);
        event.reply(message)
                .queue(queueSuccessConsumer(log), queueFailureConsumer(log));
    }

    private String requester(AudioTrack track) {
        Object data = track.getUserData();
        if (data instanceof TrackMetadata metadata) {
            return metadata.requesterName();
        }
        return "неизвестно";
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(getName(), getDescription());
    }
}
