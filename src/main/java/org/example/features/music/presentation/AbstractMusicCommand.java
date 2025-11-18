package org.example.features.music.presentation;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import org.example.commands.Command;
import org.example.features.music.application.MusicService;
import org.example.features.music.domain.TrackMetadata;

import java.time.Duration;
import java.util.EnumSet;
import java.util.Optional;

@Slf4j
public abstract class AbstractMusicCommand implements Command<SlashCommandInteractionEvent> {

    protected final MusicService musicService;

    protected AbstractMusicCommand(MusicService musicService) {
        this.musicService = musicService;
    }

    protected Optional<VoiceContext> requireVoiceContext(SlashCommandInteractionEvent event, boolean requireBotSameChannel) {
        Guild guild = event.getGuild();
        if (guild == null) {
            event.reply("Эту команду можно вызывать только внутри сервера.")
                    .setEphemeral(true)
                    .queue(queueSuccessConsumer(log), queueFailureConsumer(log));
            return Optional.empty();
        }
        Member member = event.getMember();
        if (member == null || member.getVoiceState() == null || !member.getVoiceState().inAudioChannel()) {
            event.reply("Нужно находиться в голосовом канале, чтобы пользоваться музыкой.")
                    .setEphemeral(true)
                    .queue(queueSuccessConsumer(log), queueFailureConsumer(log));
            return Optional.empty();
        }
        AudioChannelUnion channel = member.getVoiceState().getChannel();
        if (channel == null) {
            event.reply("Не удалось определить ваш голосовой канал. Попробуйте ещё раз.")
                    .setEphemeral(true)
                    .queue(queueSuccessConsumer(log), queueFailureConsumer(log));
            return Optional.empty();
        }
        Member selfMember = guild.getSelfMember();
        AudioChannelUnion botChannel = selfMember.getVoiceState() != null ? selfMember.getVoiceState().getChannel() : null;
        if (requireBotSameChannel && botChannel != null && !botChannel.equals(channel)) {
            event.reply("Музыкальные команды доступны только в том канале, где находится бот.")
                    .setEphemeral(true)
                    .queue(queueSuccessConsumer(log), queueFailureConsumer(log));
            return Optional.empty();
        }
        return Optional.of(new VoiceContext(guild, guild.getAudioManager(), member, selfMember, channel));
    }

    protected boolean ensureVoicePermissions(VoiceContext voiceContext, SlashCommandInteractionEvent event) {
        EnumSet<Permission> required = EnumSet.of(Permission.VOICE_CONNECT, Permission.VOICE_SPEAK);
        Permission[] permissions = required.toArray(new Permission[0]);
        if (!voiceContext.selfMember().hasPermission(voiceContext.channel(), permissions)) {
            event.reply("Мне не хватает прав, чтобы подключиться и говорить в этом голосовом канале.")
                    .setEphemeral(true)
                    .queue(queueSuccessConsumer(log), queueFailureConsumer(log));
            return false;
        }
        return true;
    }

    protected TrackMetadata metadataFrom(Member member) {
        return new TrackMetadata(member.getIdLong(), member.getEffectiveName());
    }

    protected String formatDuration(long millis) {
        if (millis < 0 || millis == Long.MAX_VALUE) {
            return "live";
        }
        Duration duration = Duration.ofMillis(millis);
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format("%02d:%02d", minutes, seconds);
    }

    protected record VoiceContext(Guild guild,
                                  AudioManager audioManager,
                                  Member member,
                                  Member selfMember,
                                  AudioChannelUnion channel) {
    }
}
