package org.example.commands;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ChangeRoleCommand implements Command<SlashCommandInteractionEvent> {
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
        var options = event.getOptionsByType(OptionType.ROLE);
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(getName(), getDescription())
                .addOption(OptionType.ROLE, "Changeable role", "Role that is going to be changed")
                .addOption(OptionType.ROLE, "Settable role", "Role that is going to be set");
    }
}
