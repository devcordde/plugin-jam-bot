package de.chojo.gamejam.commands.subcommands;

import de.chojo.jdautil.wrapper.SlashCommandContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public interface SubCommand {
    void execute(SlashCommandInteractionEvent event, SlashCommandContext context);
}
