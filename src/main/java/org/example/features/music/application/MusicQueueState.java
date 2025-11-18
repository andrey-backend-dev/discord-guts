package org.example.features.music.application;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.example.features.music.domain.MusicLoopMode;

import java.util.List;

public record MusicQueueState(AudioTrack nowPlaying,
                              List<AudioTrack> queuedTracks,
                              boolean paused,
                              MusicLoopMode loopMode) {
}
