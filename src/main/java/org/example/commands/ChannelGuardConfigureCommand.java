package org.example.commands;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.example.features.channelguard.ChannelGuardFeatureService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChannelGuardConfigureCommand implements Command<SlashCommandInteractionEvent> {

    private static final String TEXT_CHANNEL_OPTION = "text-channel";
    private static final String MEDIA_CHANNEL_OPTION = "media-channel";
    private static final String MUSIC_CHANNEL_OPTION = "music-channel";

    private final ChannelGuardFeatureService channelGuardFeatureService;

    @Override
    public String getName() {
        return "channel-guard-config";
    }

    @Override
    public String getDescription() {
        return "Настраивает каналы, где разрешены только текст, медиа или музыкальные команды.";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        var guild = event.getGuild();
        if (guild == null) {
            event.reply("Эту команду можно вызвать только внутри сервера.")
                    .setEphemeral(true)
                    .queue(queueSuccessConsumer(log), queueFailureConsumer(log));
            return;
        }

        GuildMessageChannel textChannel;
        GuildMessageChannel mediaChannel;
        GuildMessageChannel musicChannel;
        try {
            textChannel = resolveChannelOption(event, TEXT_CHANNEL_OPTION);
            mediaChannel = resolveChannelOption(event, MEDIA_CHANNEL_OPTION);
            musicChannel = resolveChannelOption(event, MUSIC_CHANNEL_OPTION);
        } catch (IllegalArgumentException e) {
            event.reply(e.getMessage())
                    .setEphemeral(true)
                    .queue(queueSuccessConsumer(log), queueFailureConsumer(log));
            return;
        }

        if (textChannel == null && mediaChannel == null && musicChannel == null) {
            event.reply("Нужно указать хотя бы один канал.")
                    .setEphemeral(true)
                    .queue(queueSuccessConsumer(log), queueFailureConsumer(log));
            return;
        }

        try {
            channelGuardFeatureService.updateConfiguration(guild, textChannel, mediaChannel, musicChannel);
            event.reply(buildSuccessMessage(textChannel, mediaChannel, musicChannel))
                    .setEphemeral(true)
                    .queue(queueSuccessConsumer(log), queueFailureConsumer(log));
        } catch (Exception e) {
            log.error("Failed to persist channel guard configuration for guild {}", guild.getIdLong(), e);
            event.reply("Не удалось сохранить конфигурацию. Попробуйте позже.")
                    .setEphemeral(true)
                    .queue(queueSuccessConsumer(log), queueFailureConsumer(log));
        }
    }

    @Override
    public CommandData getCommandData() {
        OptionData textChannel = new OptionData(OptionType.CHANNEL, TEXT_CHANNEL_OPTION,
                "Канал, где разрешены только текстовые сообщения", false)
                .setChannelTypes(ChannelType.TEXT, ChannelType.NEWS);
        OptionData mediaChannel = new OptionData(OptionType.CHANNEL, MEDIA_CHANNEL_OPTION,
                "Канал для медиа-сообщений", false)
                .setChannelTypes(ChannelType.TEXT, ChannelType.NEWS);
        OptionData musicChannel = new OptionData(OptionType.CHANNEL, MUSIC_CHANNEL_OPTION,
                "Канал, где допускаются только команды", false)
                .setChannelTypes(ChannelType.TEXT, ChannelType.NEWS);
        return Commands.slash(getName(), getDescription())
                .addOptions(textChannel, mediaChannel, musicChannel);
    }

    private GuildMessageChannel resolveChannelOption(SlashCommandInteractionEvent event, String optionName) {
        OptionMapping option = event.getOption(optionName);
        if (option == null) {
            return null;
        }
        var channel = option.getAsChannel();
        if (channel instanceof GuildMessageChannel messageChannel) {
            return messageChannel;
        }
        throw new IllegalArgumentException("Можно указать только текстовый канал.");
    }

    private String buildSuccessMessage(GuildMessageChannel textChannel,
                                       GuildMessageChannel mediaChannel,
                                       GuildMessageChannel musicChannel) {
        List<String> parts = new ArrayList<>();
        if (textChannel != null) {
            parts.add("текстовый — " + textChannel.getAsMention());
        }
        if (mediaChannel != null) {
            parts.add("медиа — " + mediaChannel.getAsMention());
        }
        if (musicChannel != null) {
            parts.add("музыка — " + musicChannel.getAsMention());
        }
        return "Настройки обновлены: " + String.join(", ", parts) + ".";
    }
}
