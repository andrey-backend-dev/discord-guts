package org.example.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.example.features.channelguard.ChannelGuardFeatureService;
import org.example.persistence.channelguard.ChannelGuardConfiguration;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChannelGuardMessageListener extends ListenerAdapter {

    private final ChannelGuardFeatureService channelGuardFeatureService;

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.isFromGuild() || event.getAuthor().isBot()) {
            return;
        }

        long guildId = event.getGuild().getIdLong();
        if (!channelGuardFeatureService.isFeatureEnabled(guildId)) {
            return;
        }

        ChannelGuardConfiguration configuration = channelGuardFeatureService.findConfiguration(guildId)
                .orElse(null);
        if (configuration == null) {
            return;
        }

        long channelId = event.getChannel().getIdLong();
        if (Objects.equals(configuration.getTextChannelId(), channelId)) {
            handleTextChannel(event);
        } else if (Objects.equals(configuration.getMediaChannelId(), channelId)) {
            handleMediaChannel(event);
        } else if (Objects.equals(configuration.getMusicChannelId(), channelId)) {
            handleMusicChannel(event);
        }
    }

    private void handleTextChannel(MessageReceivedEvent event) {
        Message message = event.getMessage();
        boolean hasMedia = hasMedia(message);
        boolean hasPlayCommand = startsWithPlay(message);
        if (!hasMedia && !hasPlayCommand) {
            return;
        }

        deleteMessage(message);

        if (hasPlayCommand) {
            sendStopCommand(event);
        }

        warnUser(event, "В этом канале разрешены только текстовые сообщения без медиа и /play.");
    }

    private void handleMediaChannel(MessageReceivedEvent event) {
        Message message = event.getMessage();
        if (hasMedia(message)) {
            return;
        }

        deleteMessage(message);
        warnUser(event, "В этом канале нужно прикладывать фото, видео или другие медиа-файлы.");
    }

    private void handleMusicChannel(MessageReceivedEvent event) {
        Message message = event.getMessage();
        String content = message.getContentRaw();
        if (content != null && content.startsWith("/")) {
            return;
        }

        deleteMessage(message);
        warnUser(event, "Здесь можно отправлять только команды, начинающиеся со символа '/'.");
    }

    private void deleteMessage(Message message) {
        message.delete().queue(
                __ -> {},
                error -> log.warn("Failed to delete message {}: {}", message.getId(), error.getMessage())
        );
    }

    private void warnUser(MessageReceivedEvent event, String warning) {
        event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", " + warning).queue(
                __ -> {},
                error -> log.warn("Failed to send warning in guild {}: {}", event.getGuild().getId(), error.getMessage())
        );
    }

    private boolean hasMedia(Message message) {
        return !message.getAttachments().isEmpty()
                || !message.getEmbeds().isEmpty()
                || !message.getStickers().isEmpty();
    }

    private boolean startsWithPlay(Message message) {
        String content = message.getContentRaw();
        return content != null && content.trim().toLowerCase().startsWith("/play");
    }

    private void sendStopCommand(MessageReceivedEvent event) {
        String stopPayload = event.getMessage().getMentions().getMembers().stream()
                .filter(member -> member.getUser().isBot())
                .findFirst()
                .map(Member::getAsMention)
                .map(mention -> mention + " /stop")
                .orElse("/stop");
        event.getChannel().sendMessage(stopPayload).queue(
                __ -> {},
                error -> log.warn("Failed to notify music bot in guild {}: {}", event.getGuild().getId(), error.getMessage())
        );
    }
}
