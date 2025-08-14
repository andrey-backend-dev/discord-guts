package org.example.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.example.commands.Command;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CommandListener extends ListenerAdapter {

    private Map<String, Command<?>> commandMap;

    CommandListener(List<Command<?>> commands) {
        commandMap = commands.stream().collect(Collectors.toMap(Command::getName, Function.identity()));
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        log.info("Command listener is ready.");
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        Command<SlashCommandInteractionEvent> command = (Command<SlashCommandInteractionEvent>) commandMap.get(event.getName());

        if (command == null) {
            log.error("There is no such command registered at commands package. Command name: {}", event.getName());
            return;
        }

        command.execute(event);
    }

}
