package org.example.commands;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Role;
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
public class ChangeRoleCommand implements Command<SlashCommandInteractionEvent> {

    private static final String CHANGEABLE_ROLE_OPTION = "changeable-role";
    private static final String SETTABLE_ROLE_OPTION = "settable-role";

    private final ChangeRoleFeatureService changeRoleFeatureService;
    @Override
    public String getName() {
        return "change-role";
    }

    @Override
    public String getDescription() {
        return "Command that registers change role feature.";
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

        Role changeableRole = getRoleOption(event, CHANGEABLE_ROLE_OPTION);
        Role settableRole = getRoleOption(event, SETTABLE_ROLE_OPTION);

        if (changeableRole == null || settableRole == null) {
            event.reply("Не удалось определить роли для смены. Попробуйте ещё раз.")
                    .setEphemeral(true)
                    .queue(queueSuccessConsumer(log), queueFailureConsumer(log));
            return;
        }

        if (changeableRole.getIdLong() == settableRole.getIdLong()) {
            event.reply("Роли для смены должны быть разными.")
                    .setEphemeral(true)
                    .queue(queueSuccessConsumer(log), queueFailureConsumer(log));
            return;
        }

        try {
            changeRoleFeatureService.registerConfiguration(guild, changeableRole, settableRole);
            event.reply(String.format(
                            "Ротация ролей настроена: %s -> %s.",
                            changeableRole.getAsMention(), settableRole.getAsMention()
                    ))
                    .setEphemeral(true)
                    .queue(queueSuccessConsumer(log), queueFailureConsumer(log));
        } catch (Exception e) {
            log.error("Failed to persist change role configuration for guild {}", guild.getIdLong(), e);
            event.reply("Не удалось сохранить конфигурацию смены ролей. Попробуйте позже.")
                    .setEphemeral(true)
                    .queue(queueSuccessConsumer(log), queueFailureConsumer(log));
        }
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(getName(), getDescription())
                .addOption(OptionType.ROLE, CHANGEABLE_ROLE_OPTION, "Роль, участники которой будут ротироваться", true)
                .addOption(OptionType.ROLE, SETTABLE_ROLE_OPTION, "Роль, которую ежедневно получает выбранный участник", true);
    }

    private Role getRoleOption(SlashCommandInteractionEvent event, String optionName) {
        OptionMapping option = event.getOption(optionName);
        return option != null ? option.getAsRole() : null;
    }
}
