package org.example.features.music.presentation;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.example.features.music.application.MusicService;
import org.example.features.music.domain.MusicLoopMode;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LoopMusicCommand extends AbstractMusicCommand {

    private static final String MODE_OPTION = "mode";

    public LoopMusicCommand(MusicService musicService) {
        super(musicService);
    }

    @Override
    public String getName() {
        return "loop";
    }

    @Override
    public String getDescription() {
        return "Настроить повтор треков";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        var modeOption = event.getOption(MODE_OPTION);
        if (modeOption == null) {
            event.reply("Нужно выбрать режим повтора: off, track или queue.")
                    .setEphemeral(true)
                    .queue(queueSuccessConsumer(log), queueFailureConsumer(log));
            return;
        }
        var voiceContextOpt = requireVoiceContext(event, true);
        if (voiceContextOpt.isEmpty()) {
            return;
        }
        long guildId = voiceContextOpt.get().guild().getIdLong();
        MusicLoopMode mode = parseMode(modeOption);
        MusicLoopMode previous = musicService.updateLoopMode(guildId, mode);
        event.reply(String.format("Режим повтора: %s (раньше был %s).", label(mode), label(previous)))
                .queue(queueSuccessConsumer(log), queueFailureConsumer(log));
    }

    private MusicLoopMode parseMode(OptionMapping optionMapping) {
        String value = optionMapping.getAsString();
        return switch (value.toLowerCase()) {
            case "track" -> MusicLoopMode.TRACK;
            case "queue" -> MusicLoopMode.QUEUE;
            default -> MusicLoopMode.OFF;
        };
    }

    private String label(MusicLoopMode mode) {
        return switch (mode) {
            case OFF -> "выключен";
            case TRACK -> "текущий трек";
            case QUEUE -> "вся очередь";
        };
    }

    @Override
    public CommandData getCommandData() {
        OptionData mode = new OptionData(OptionType.STRING, MODE_OPTION, "off | track | queue", true)
                .addChoice("off", "off")
                .addChoice("track", "track")
                .addChoice("queue", "queue");
        return Commands.slash(getName(), getDescription()).addOptions(mode);
    }
}
