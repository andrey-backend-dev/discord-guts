package org.example.features.music.domain;

import java.util.Objects;

public record TrackMetadata(long requesterId,
                            String requesterName,
                            String trackTitle,
                            String trackAuthor,
                            String trackUrl,
                            long trackDurationMillis) {

    public TrackMetadata(long requesterId, String requesterName) {
        this(requesterId, requesterName, null, null, null, -1L);
    }

    public TrackMetadata withTrackInfo(String title, String author, String url, long durationMillis) {
        return new TrackMetadata(requesterId, requesterName, title, author, url, durationMillis);
    }

    public String titleOr(String fallback) {
        return trackTitle != null && !trackTitle.isBlank() ? trackTitle : fallback;
    }

    public String authorOr(String fallback) {
        return trackAuthor != null && !trackAuthor.isBlank() ? trackAuthor : fallback;
    }

    public long durationOr(long fallback) {
        return trackDurationMillis >= 0 ? trackDurationMillis : fallback;
    }

    public String urlOr(String fallback) {
        return trackUrl != null && !trackUrl.isBlank() ? trackUrl : fallback;
    }

    public boolean hasTrackInfo() {
        return Objects.nonNull(trackTitle);
    }
}
