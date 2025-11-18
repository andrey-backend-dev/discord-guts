package org.example.features.music.application;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.features.music.domain.GuildMusicManager;
import org.example.features.music.domain.LavaPlayerSendHandler;
import org.example.features.music.domain.MusicLoopMode;
import org.example.features.music.domain.TrackMetadata;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class MusicService {

    private static final Pattern URL_PATTERN = Pattern.compile("^(https?://).+", Pattern.CASE_INSENSITIVE);

    private final AudioPlayerManager audioPlayerManager;
    private final Map<Long, GuildMusicManager> musicManagers = new ConcurrentHashMap<>();

    public CompletableFuture<MusicPlayRequestResult> loadAndPlay(long guildId, String rawQuery, TrackMetadata metadata) {
        String query = rawQuery == null ? "" : rawQuery.trim();
        if (query.isBlank()) {
            return CompletableFuture.completedFuture(MusicPlayRequestResult.failure("Нужно указать ссылку или поисковый запрос."));
        }
        GuildMusicManager guildMusicManager = getOrCreateGuildManager(guildId);
        CompletableFuture<MusicPlayRequestResult> future = new CompletableFuture<>();
        String identifier = resolveIdentifier(query);
        audioPlayerManager.loadItemOrdered(guildMusicManager, identifier, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                handleSingleTrack(guildMusicManager, track, metadata, future, 0);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                if (playlist.isSearchResult()) {
                    AudioTrack first = playlist.getTracks().isEmpty() ? null : playlist.getTracks().get(0);
                    if (first == null) {
                        future.complete(MusicPlayRequestResult.failure("Ничего не найдено по запросу."));
                        return;
                    }
                    handleSingleTrack(guildMusicManager, first, metadata, future, 0);
                    return;
                }
                LinkedList<AudioTrack> orderedTracks = orderPlaylistTracks(playlist);
                if (orderedTracks.isEmpty()) {
                    future.complete(MusicPlayRequestResult.failure("Плейлист не содержит треков."));
                    return;
                }
                AudioTrack firstTrack = orderedTracks.removeFirst();
                handlePlaylist(guildMusicManager, firstTrack, orderedTracks, metadata, future);
            }

            @Override
            public void noMatches() {
                future.complete(MusicPlayRequestResult.failure("Ничего не найдено для '" + rawQuery + "'."));
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                log.warn("Failed to load track for guild {}", guildId, exception);
                future.complete(MusicPlayRequestResult.failure("Не удалось загрузить трек: " + exception.getMessage()));
            }
        });
        return future;
    }

    public Optional<AudioTrack> skip(long guildId) {
        GuildMusicManager manager = musicManagers.get(guildId);
        if (manager == null) {
            return Optional.empty();
        }
        return manager.getScheduler().skip();
    }

    public boolean stop(long guildId) {
        GuildMusicManager manager = musicManagers.get(guildId);
        if (manager == null) {
            return false;
        }
        return manager.getScheduler().stop();
    }

    public boolean pause(long guildId) {
        GuildMusicManager manager = musicManagers.get(guildId);
        if (manager == null) {
            return false;
        }
        if (manager.getPlayer().getPlayingTrack() == null || manager.getPlayer().isPaused()) {
            return false;
        }
        manager.getPlayer().setPaused(true);
        return true;
    }

    public boolean resume(long guildId) {
        GuildMusicManager manager = musicManagers.get(guildId);
        if (manager == null) {
            return false;
        }
        if (manager.getPlayer().getPlayingTrack() == null || !manager.getPlayer().isPaused()) {
            return false;
        }
        manager.getPlayer().setPaused(false);
        return true;
    }

    public Optional<AudioTrack> nowPlaying(long guildId) {
        GuildMusicManager manager = musicManagers.get(guildId);
        if (manager == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(manager.getPlayer().getPlayingTrack());
    }

    public Optional<MusicQueueState> getQueueState(long guildId) {
        GuildMusicManager manager = musicManagers.get(guildId);
        if (manager == null) {
            return Optional.empty();
        }
        return Optional.of(new MusicQueueState(
                manager.getPlayer().getPlayingTrack(),
                List.copyOf(manager.getScheduler().getQueueSnapshot()),
                manager.getPlayer().isPaused(),
                manager.getScheduler().getLoopMode()
        ));
    }

    public MusicLoopMode updateLoopMode(long guildId, MusicLoopMode loopMode) {
        GuildMusicManager manager = getOrCreateGuildManager(guildId);
        MusicLoopMode previous = manager.getScheduler().getLoopMode();
        manager.getScheduler().setLoopMode(loopMode);
        return previous;
    }

    public MusicLoopMode getLoopMode(long guildId) {
        GuildMusicManager manager = musicManagers.get(guildId);
        return manager != null ? manager.getScheduler().getLoopMode() : MusicLoopMode.OFF;
    }

    public void leaveGuild(long guildId) {
        GuildMusicManager manager = musicManagers.remove(guildId);
        if (manager != null) {
            manager.getScheduler().stop();
        }
    }

    public LavaPlayerSendHandler getSendHandler(long guildId) {
        return getOrCreateGuildManager(guildId).getSendHandler();
    }

    public GuildMusicManager getOrCreateGuildManager(long guildId) {
        return musicManagers.computeIfAbsent(guildId, id -> new GuildMusicManager(audioPlayerManager));
    }

    private void handleSingleTrack(GuildMusicManager manager, AudioTrack track, TrackMetadata metadata,
                                   CompletableFuture<MusicPlayRequestResult> future, int additionalTracks) {
        track.setUserData(metadata);
        int position = manager.getScheduler().enqueue(track);
        boolean startedImmediately = position == 0;
        future.complete(MusicPlayRequestResult.success(track.getInfo(), startedImmediately, position, additionalTracks));
    }

    private void handlePlaylist(GuildMusicManager manager, AudioTrack firstTrack, List<AudioTrack> remaining,
                                TrackMetadata metadata, CompletableFuture<MusicPlayRequestResult> future) {
        firstTrack.setUserData(metadata);
        int position = manager.getScheduler().enqueue(firstTrack);
        for (AudioTrack track : remaining) {
            track.setUserData(metadata);
            manager.getScheduler().enqueue(track);
        }
        future.complete(MusicPlayRequestResult.success(firstTrack.getInfo(), position == 0, position, remaining.size()));
    }

    private LinkedList<AudioTrack> orderPlaylistTracks(AudioPlaylist playlist) {
        LinkedList<AudioTrack> tracks = new LinkedList<>(playlist.getTracks());
        AudioTrack selected = playlist.getSelectedTrack();
        if (selected != null && tracks.remove(selected)) {
            tracks.addFirst(selected);
        }
        return tracks;
    }

    private String resolveIdentifier(String query) {
        return URL_PATTERN.matcher(query).matches() ? query : "ytsearch:" + query;
    }
}
