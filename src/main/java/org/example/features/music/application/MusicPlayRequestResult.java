package org.example.features.music.application;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

public record MusicPlayRequestResult(
        boolean success,
        AudioTrackInfo trackInfo,
        boolean startedImmediately,
        int queuePosition,
        int additionalTracks,
        String errorMessage
) {

    public static MusicPlayRequestResult failure(String message) {
        return new MusicPlayRequestResult(false, null, false, 0, 0, message);
    }

    public static MusicPlayRequestResult success(AudioTrackInfo trackInfo, boolean startedImmediately,
                                                 int queuePosition, int additionalTracks) {
        return new MusicPlayRequestResult(true, trackInfo, startedImmediately, queuePosition, additionalTracks, null);
    }
}
