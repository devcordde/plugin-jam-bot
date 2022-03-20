package de.chojo.gamejam.commands.subcommands;

import de.chojo.gamejam.data.JamData;
import de.chojo.gamejam.util.Future;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public record JamAdminStart(JamData jamData) implements SubCommand {

    @Override
    public void execute(SlashCommandInteractionEvent event, SlashCommandContext context) {
        jamData.getActiveJam(event.getGuild()).thenAccept(jam -> {
            if (jam.isPresent()) {
                event.reply("A jam is already active.").setEphemeral(true).queue();
                return;
            }
            var nextJam = jamData.getNextOrCurrentJam(event.getGuild()).join();
            if (nextJam.isEmpty()) {
                event.reply("There is no upcoming jam.").queue();
                return;
            }
            nextJam.get().state().active(true);
            jamData.updateJamState(jam.get());
            event.reply("Jam state changed to active").queue();
        }).whenComplete(Future.error());
    }
}
