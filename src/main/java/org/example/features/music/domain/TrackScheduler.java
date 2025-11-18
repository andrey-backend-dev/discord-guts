package org.example.features.music.domain;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class TrackScheduler extends AudioEventAdapter {

    private final AudioPlayer player;
    private final Queue<AudioTrack> queue = new LinkedList<>();
    private MusicLoopMode loopMode = MusicLoopMode.OFF;

    public TrackScheduler(AudioPlayer player) {
        this.player = player;
    }

    public synchronized int enqueue(AudioTrack track) {
        if (player.startTrack(track, true)) {
            return 0;
        }
        int position = queue.size() + 1;
        queue.offer(track);
        return position;
    }

    public synchronized List<AudioTrack> getQueueSnapshot() {
        return new ArrayList<>(queue);
    }

    public synchronized AudioTrack getCurrentTrack() {
        return player.getPlayingTrack();
    }

    public synchronized void setLoopMode(MusicLoopMode loopMode) {
        this.loopMode = loopMode;
    }

    public synchronized MusicLoopMode getLoopMode() {
        return loopMode;
    }

    public synchronized boolean stop() {
        boolean hadPlayback = player.getPlayingTrack() != null || !queue.isEmpty();
        queue.clear();
        player.stopTrack();
        loopMode = MusicLoopMode.OFF;
        return hadPlayback;
    }

    public synchronized java.util.Optional<AudioTrack> skip() {
        AudioTrack current = player.getPlayingTrack();
        if (current == null) {
            return java.util.Optional.empty();
        }
        AudioTrack next = queue.poll();
        if (next != null) {
            player.startTrack(next, false);
        } else {
            player.stopTrack();
        }
        return java.util.Optional.of(current);
    }

    @Override
    public synchronized void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (!endReason.mayStartNext) {
            return;
        }
        if (loopMode == MusicLoopMode.TRACK) {
            player.startTrack(cloneWithMetadata(track), false);
            return;
        }
        if (loopMode == MusicLoopMode.QUEUE) {
            queue.offer(cloneWithMetadata(track));
        }
        AudioTrack nextTrack = queue.poll();
        if (nextTrack != null) {
            player.startTrack(nextTrack, false);
        }
    }

    private AudioTrack cloneWithMetadata(AudioTrack track) {
        AudioTrack clone = track.makeClone();
        clone.setUserData(track.getUserData());
        return clone;
    }
}
