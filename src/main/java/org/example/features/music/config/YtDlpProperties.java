package org.example.features.music.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "music.youtube.ytdlp")
public record YtDlpProperties(boolean enabled, String executable, Duration timeout) {

    public YtDlpProperties() {
        this(true, "yt-dlp", Duration.ofSeconds(20));
    }

    public YtDlpProperties {
        if (executable == null || executable.isBlank()) {
            executable = "yt-dlp";
        }
        if (timeout == null || timeout.isZero() || timeout.isNegative()) {
            timeout = Duration.ofSeconds(20);
        }
    }
}
