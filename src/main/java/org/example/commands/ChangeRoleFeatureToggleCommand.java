package org.example.commands;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.example.features.changerole.ChangeRoleFeatureService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChangeRoleFeatureToggleCommand implements Command<SlashCommandInteractionEvent> {

    private static final String ENABLED_OPTION = "enabled";

    private final ChangeRoleFeatureService changeRoleFeatureService;

    @Override
    public String getName() {
        return "change-role-toggle";
    }

    @Override
    public String getDescription() {
        return "Включает или выключает смену ролей на текущем сервере.";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        var guild = event.getGuild();
        if (guild == null) {
            event.reply("Эту команду можно вызывать только внутри сервера.")
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
            changeRoleFeatureService.setFeatureEnabled(guild, enabled);
            String message = enabled
                    ? "Смена ролей снова активирована для этого сервера."
                    : "Смена ролей отключена для этого сервера.";
            event.reply(message)
                    .setEphemeral(true)
                    .queue(queueSuccessConsumer(log), queueFailureConsumer(log));
        } catch (Exception e) {
            log.error("Failed to toggle change-role feature for guild {}", guild.getIdLong(), e);
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
