package org.example.commands;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EchoCommand implements Command<SlashCommandInteractionEvent> {

    private static final String TEXT_OPTION = "text";
    private static final String TEXT_DESCRIPTION = "The text to echo";

    @Override
    public String getName() {
        return "echo";
    }

    @Override
    public String getDescription() {
        return "Command that repeats user's input";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        OptionMapping textOption = event.getOption(TEXT_OPTION);
        if (textOption == null) {
            event.reply("You have to enter the text to see the reply.").queue(
                    queueSuccessConsumer(log), queueFailureConsumer(log)
            );
            return;
        }
        event.reply(textOption.getAsString()).queue(
                queueSuccessConsumer(log), queueFailureConsumer(log)
        );
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(getName(), getDescription())
                .addOption(OptionType.STRING, TEXT_OPTION, TEXT_DESCRIPTION, true);
    }
}
