package de.chojo.gamejam.commands.settings;

import de.chojo.gamejam.commands.SettingsSubcommand;
import de.chojo.gamejam.data.JamData;
import de.chojo.gamejam.data.wrapper.jam.JamSettings;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public record Role(JamData jamData) implements SettingsSubcommand {

    @Override
    public void execute(SlashCommandInteractionEvent event, SlashCommandContext context, JamSettings settings) {
        settings.jamRole(event.getOption("role").getAsRole().getIdLong());
        jamData.updateSettings(event.getGuild(), settings)
                .thenRun(() -> event.reply("Updated settings").setEphemeral(true).queue());
    }
}
