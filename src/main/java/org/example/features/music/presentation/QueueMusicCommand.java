package org.example.features.music.presentation;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.example.features.music.application.MusicQueueState;
import org.example.features.music.application.MusicService;
import org.example.features.music.domain.MusicLoopMode;
import org.example.features.music.domain.TrackMetadata;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class QueueMusicCommand extends AbstractMusicCommand {

    private static final int MAX_LINES = 10;

    public QueueMusicCommand(MusicService musicService) {
        super(musicService);
    }

    @Override
    public String getName() {
        return "queue";
    }

    @Override
    public String getDescription() {
        return "Показать текущую очередь воспроизведения";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        var voiceContextOpt = requireVoiceContext(event, true);
        if (voiceContextOpt.isEmpty()) {
            return;
        }
        long guildId = voiceContextOpt.get().guild().getIdLong();
        var stateOpt = musicService.getQueueState(guildId);
        if (stateOpt.isEmpty()) {
            event.reply("Очередь пуста.")
                    .setEphemeral(true)
                    .queue(queueSuccessConsumer(log), queueFailureConsumer(log));
            return;
        }
        var state = stateOpt.get();
        if (state.nowPlaying() == null && state.queuedTracks().isEmpty()) {
            event.reply("Очередь пуста.")
                    .setEphemeral(true)
                    .queue(queueSuccessConsumer(log), queueFailureConsumer(log));
            return;
        }

        StringBuilder builder = new StringBuilder();
        appendNowPlaying(state, builder);
        appendQueue(state.queuedTracks(), builder);
        builder.append("Режим повтора: ").append(loopLabel(state.loopMode())).append('.');
        if (state.paused()) {
            builder.append(" Музыка сейчас на паузе.");
        }

        event.reply(builder.toString())
                .queue(queueSuccessConsumer(log), queueFailureConsumer(log));
    }

    private void appendNowPlaying(MusicQueueState state, StringBuilder builder) {
        AudioTrack now = state.nowPlaying();
        if (now == null) {
            builder.append("Сейчас ничего не играет.\n");
            return;
        }
        builder.append("Сейчас играет: ")
                .append('[').append(formatDuration(now.getDuration())).append("] ")
                .append(now.getInfo().title)
                .append(" — ")
                .append(requester(now))
                .append('\n');
    }

    private void appendQueue(List<AudioTrack> tracks, StringBuilder builder) {
        if (tracks.isEmpty()) {
            builder.append("Очередь пуста.\n");
            return;
        }
        builder.append("Очередь:\n");
        for (int i = 0; i < tracks.size() && i < MAX_LINES; i++) {
            AudioTrack track = tracks.get(i);
            builder.append(i + 1)
                    .append('.').append(' ')
                    .append('[').append(formatDuration(track.getDuration())).append("] ")
                    .append(track.getInfo().title)
                    .append(" — ")
                    .append(requester(track))
                    .append('\n');
        }
        if (tracks.size() > MAX_LINES) {
            builder.append("…и ещё ").append(tracks.size() - MAX_LINES).append(" трек(ов).\n");
        }
    }

    private String loopLabel(MusicLoopMode mode) {
        return switch (mode) {
            case TRACK -> "повтор трека";
            case QUEUE -> "повтор очереди";
            case OFF -> "выключен";
        };
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
