package org.example.features.music.domain;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

public class GuildMusicManager {

    private final AudioPlayer player;
    private final TrackScheduler scheduler;
    private final LavaPlayerSendHandler sendHandler;

    public GuildMusicManager(AudioPlayerManager audioPlayerManager) {
        this.player = audioPlayerManager.createPlayer();
        this.scheduler = new TrackScheduler(player);
        this.player.addListener(scheduler);
        this.sendHandler = new LavaPlayerSendHandler(player);
    }

    public AudioPlayer getPlayer() {
        return player;
    }

    public TrackScheduler getScheduler() {
        return scheduler;
    }

    public LavaPlayerSendHandler getSendHandler() {
        return sendHandler;
    }
}
