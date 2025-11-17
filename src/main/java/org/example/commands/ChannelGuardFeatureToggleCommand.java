package org.example.commands;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.example.features.channelguard.ChannelGuardFeatureService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChannelGuardFeatureToggleCommand implements Command<SlashCommandInteractionEvent> {

    private static final String ENABLED_OPTION = "enabled";

    private final ChannelGuardFeatureService channelGuardFeatureService;

    @Override
    public String getName() {
        return "channel-guard-toggle";
    }

    @Override
    public String getDescription() {
        return "Включает или выключает фильтрацию чатов по типу сообщений.";
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

        OptionMapping enabledOption = event.getOption(ENABLED_OPTION);
        if (enabledOption == null) {
            event.reply("Не удалось определить состояние фичи. Попробуйте ещё раз.")
                    .setEphemeral(true)
                    .queue(queueSuccessConsumer(log), queueFailureConsumer(log));
            return;
        }

        boolean enabled = enabledOption.getAsBoolean();
        try {
            channelGuardFeatureService.setFeatureEnabled(guild, enabled);
            String message = enabled
                    ? "Ограничения по типу сообщений включены для этого сервера."
                    : "Ограничения по типу сообщений отключены для этого сервера.";
            event.reply(message)
                    .setEphemeral(true)
                    .queue(queueSuccessConsumer(log), queueFailureConsumer(log));
        } catch (Exception e) {
            log.error("Failed to toggle channel guard feature for guild {}", guild.getIdLong(), e);
            event.reply("Не удалось обновить состояние фичи. Попробуйте позже.")
                    .setEphemeral(true)
                    .queue(queueSuccessConsumer(log), queueFailureConsumer(log));
        }
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(getName(), getDescription())
                .addOption(OptionType.BOOLEAN, ENABLED_OPTION, "Укажите 'false', чтобы отключить фичу", true);
    }
}
