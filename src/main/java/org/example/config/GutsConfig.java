package org.example.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.example.commands.Command;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.EnumSet;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class GutsConfig {

    @Getter
    @Value("${token}")
    private String token;
    private final List<ListenerAdapter> listeners;
    private final List<Command<?>> commands;

    @Bean
    public JDA jda() {
        JDA jda = null;

        try {
            jda = JDABuilder.createDefault(token)
                    .enableIntents(EnumSet.allOf(GatewayIntent.class))
                    .addEventListeners(listeners.toArray())
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .build();
            jda.awaitReady();
            log.info("JDA instance created successfully.");
            jda.updateCommands().addCommands(commands.stream().map(Command::getCommandData).toList()).queue(
                    _ -> log.info("Commands registered successfully."),
                    failure -> log.error("Commands creation failure. ", failure)
            );
        } catch (Exception e) {
            log.error("JDA instance creation failure. ", e);
        }

        return jda;
    }
}
