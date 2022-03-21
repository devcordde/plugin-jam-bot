package de.chojo.gamejam.commands;

import de.chojo.jdautil.wrapper.SlashCommandContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public interface SubCommand<T> {
    void execute(SlashCommandInteractionEvent event, SlashCommandContext context, T data);

    interface Nonce extends SubCommand<Void> {
        default void execute(SlashCommandInteractionEvent event, SlashCommandContext context, Void nonce){
            execute(event, context);
        }
        void execute(SlashCommandInteractionEvent event, SlashCommandContext context);
    }
}
