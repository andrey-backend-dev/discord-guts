package org.example.features.music.presentation;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.example.features.music.application.MusicPlayRequestResult;
import org.example.features.music.application.MusicService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PlayMusicCommand extends AbstractMusicCommand {

    private static final String QUERY_OPTION = "query";

    public PlayMusicCommand(MusicService musicService) {
        super(musicService);
    }

    @Override
    public String getName() {
        return "play";
    }

    @Override
    public String getDescription() {
        return "Воспроизвести трек или добавить его в очередь";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        OptionMapping queryOption = event.getOption(QUERY_OPTION);
        if (queryOption == null || queryOption.getAsString().isBlank()) {
            event.reply("Нужно указать ссылку или поисковый запрос.")
                    .setEphemeral(true)
                    .queue(queueSuccessConsumer(log), queueFailureConsumer(log));
            return;
        }
        var voiceContextOpt = requireVoiceContext(event, true);
        if (voiceContextOpt.isEmpty()) {
            return;
        }
        var voiceContext = voiceContextOpt.get();
        if (!ensureVoicePermissions(voiceContext, event)) {
            return;
        }

        event.deferReply().queue();

        var audioManager = voiceContext.audioManager();
        long guildId = voiceContext.guild().getIdLong();
        audioManager.setSendingHandler(musicService.getSendHandler(guildId));
        if (!audioManager.isConnected()) {
            audioManager.openAudioConnection(voiceContext.channel());
        }

        String query = queryOption.getAsString();
        var metadata = metadataFrom(voiceContext.member());
        musicService.loadAndPlay(guildId, query, metadata)
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        log.error("Не удалось обработать запрос воспроизведения для гильдии {}", guildId, throwable);
                        event.getHook().sendMessage("Произошла ошибка при загрузке трека. Попробуйте позже.")
                                .queue(queueSuccessConsumer(log), queueFailureConsumer(log));
                        return;
                    }
                    handlePlayResult(event, result);
                });
    }

    private void handlePlayResult(SlashCommandInteractionEvent event, MusicPlayRequestResult result) {
        if (!result.success()) {
            event.getHook().sendMessage(result.errorMessage())
                    .setEphemeral(true)
                    .queue(queueSuccessConsumer(log), queueFailureConsumer(log));
            return;
        }
        StringBuilder response = new StringBuilder();
        String title = result.trackInfo().title;
        String duration = formatDuration(result.trackInfo().length);
        if (result.startedImmediately()) {
            response.append("▶️ Сейчас играет: **").append(title).append("** (`").append(duration).append("`).");
        } else {
            response.append("Добавлено в очередь #").append(result.queuePosition()).append(": **")
                    .append(title).append("** (`").append(duration).append("`).");
        }
        if (result.additionalTracks() > 0) {
            response.append(" Плюс ещё ").append(result.additionalTracks()).append(" трек(ов) из плейлиста.");
        }
        event.getHook().sendMessage(response.toString())
                .queue(queueSuccessConsumer(log), queueFailureConsumer(log));
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(getName(), getDescription())
                .addOption(OptionType.STRING, QUERY_OPTION, "Ссылка или поисковый запрос", true);
    }
}
