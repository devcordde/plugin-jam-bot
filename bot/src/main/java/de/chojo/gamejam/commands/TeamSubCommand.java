package de.chojo.gamejam.commands;

import de.chojo.gamejam.data.wrapper.jam.Jam;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@FunctionalInterface
public interface TeamSubCommand {
    void execute(SlashCommandInteractionEvent event, SlashCommandContext context, Jam jam);
}
