package org.example.features.music.infrastructure.youtube;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.features.music.config.YtDlpProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

@Slf4j
@Component
@RequiredArgsConstructor
public class YoutubeAudioResolver {

    private static final Pattern YOUTUBE_HOST_PATTERN = Pattern.compile(
            "^(https?://)?([a-z0-9]+\\.)?(youtube\\.com|youtu\\.be)/.+",
            Pattern.CASE_INSENSITIVE
    );

    private final YtDlpProperties properties;
    private final ObjectMapper objectMapper;

    public Optional<ResolvedYoutubeTrack> resolve(String rawQuery) {
        if (!properties.enabled()) {
            return Optional.empty();
        }
        String query = rawQuery == null ? "" : rawQuery.trim();
        if (query.isBlank()) {
            return Optional.empty();
        }
        boolean isUrl = YOUTUBE_HOST_PATTERN.matcher(query).matches();
        if (!isUrl && query.startsWith("http")) {
            return Optional.empty();
        }
        String target = isUrl ? query : "ytsearch1:" + query;
        try {
            return execute(target);
        } catch (IOException e) {
            log.warn("Не удалось запустить yt-dlp: {}", e.getMessage());
            return Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Ожидание yt-dlp было прервано", e);
            return Optional.empty();
        }
    }

    private Optional<ResolvedYoutubeTrack> execute(String target) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add(properties.executable());
        command.add("-J");
        command.add("--skip-download");
        command.add("--no-warnings");
        command.add("--");
        command.add(target);
        Process process = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .start();
        Duration timeout = properties.timeout();
        String output;
        try (InputStream stream = process.getInputStream()) {
            output = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
        boolean finished = process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
        if (!finished) {
            process.destroyForcibly();
            log.warn("yt-dlp превысил лимит {}", timeout);
            return Optional.empty();
        }
        if (process.exitValue() != 0) {
            log.warn("yt-dlp завершился с кодом {} и сообщением: {}", process.exitValue(), output);
            return Optional.empty();
        }
        return parseOutput(output);
    }

    private Optional<ResolvedYoutubeTrack> parseOutput(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            if (root == null) {
                return Optional.empty();
            }
            if ("playlist".equals(root.path("_type").asText()) && root.has("entries")) {
                for (JsonNode entry : root.path("entries")) {
                    var resolved = toTrack(entry);
                    if (resolved.isPresent()) {
                        return resolved;
                    }
                }
                return Optional.empty();
            }
            return toTrack(root);
        } catch (IOException e) {
            log.warn("Не удалось разобрать ответ yt-dlp", e);
            return Optional.empty();
        }
    }

    private Optional<ResolvedYoutubeTrack> toTrack(JsonNode node) {
        if (node == null || node.isMissingNode()) {
            return Optional.empty();
        }
        JsonNode formats = node.path("formats");
        JsonNode bestAudio = selectBestAudioNode(formats).orElse(null);
        if (bestAudio == null) {
            String direct = node.path("url").asText(null);
            if (direct == null || direct.isBlank()) {
                return Optional.empty();
            }
            bestAudio = objectMapper.createObjectNode().put("url", direct);
        }
        String streamUrl = bestAudio.path("url").asText(null);
        if (streamUrl == null || streamUrl.isBlank()) {
            return Optional.empty();
        }
        String title = node.path("title").asText("Unknown title");
        String author = firstNonBlank(node.path("uploader").asText(null),
                node.path("channel").asText(null), "Unknown author");
        boolean isLive = node.path("is_live").asBoolean(false);
        long duration = isLive ? Long.MAX_VALUE : (long) Math.max(0, node.path("duration").asDouble(0) * 1000);
        String id = node.path("id").asText(streamUrl);
        String pageUrl = firstNonBlank(node.path("webpage_url").asText(null),
                node.path("original_url").asText(null),
                "https://www.youtube.com/watch?v=" + id);
        AudioTrackInfo info = new AudioTrackInfo(title, author, duration, id, isLive, pageUrl);
        return Optional.of(new ResolvedYoutubeTrack(streamUrl, info));
    }

    private Optional<JsonNode> selectBestAudioNode(JsonNode formats) {
        if (formats == null || !formats.isArray()) {
            return Optional.empty();
        }
        return StreamSupport.stream(formats.spliterator(), false)
                .filter(node -> "none".equals(node.path("vcodec").asText("")))
                .filter(node -> node.hasNonNull("url"))
                .max(Comparator.comparingDouble(node -> node.path("abr").asDouble(node.path("tbr").asDouble(0))));
    }

    private String firstNonBlank(String primary, String secondary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        if (secondary != null && !secondary.isBlank()) {
            return secondary;
        }
        return fallback;
    }

    public record ResolvedYoutubeTrack(String streamUrl, AudioTrackInfo trackInfo) {
    }
}
