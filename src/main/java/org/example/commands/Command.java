package org.example.commands;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

public interface Command<T extends GenericEvent> {

    String getName();

    String getDescription();

    void execute(T event);

    CommandData getCommandData();

    default Consumer<? super InteractionHook> queueSuccessConsumer(Logger log) {
        return _ -> log.info("{} command interacted successfully.", getName());
    }

    default Consumer<? super Throwable> queueFailureConsumer(Logger log) {
        return failure -> log.error("{} command interaction failure.", getName(), failure);
    }

}
