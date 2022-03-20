package de.chojo.gamejam.commands.settings;

import de.chojo.gamejam.data.wrapper.jam.JamSettings;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public interface SettingsSubcommand {

    void execute(SlashCommandInteractionEvent event, SlashCommandContext context, JamSettings settings);
}
