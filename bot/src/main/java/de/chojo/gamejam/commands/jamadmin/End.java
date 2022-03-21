package de.chojo.gamejam.commands.jamadmin;

import de.chojo.gamejam.commands.SubCommand;
import de.chojo.gamejam.data.JamData;
import de.chojo.gamejam.util.Future;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public record End(JamData jamData) implements SubCommand.Nonce {

    @Override
    public void execute(SlashCommandInteractionEvent event, SlashCommandContext context) {
        if (!event.getOption("confirm").getAsBoolean()) {
            event.reply("Please confirm").queue();
            return;
        }

        jamData.getActiveJam(event.getGuild()).thenAccept(jam -> {
            if (jam.isEmpty()) {
                event.reply("There is no active jam.").queue();
                return;
            }
            jam.get().finish(event.getGuild());
            jamData.updateJamState(jam.get());
            event.reply("Jam ended.").queue();

        }).whenComplete(Future.error());
    }
}
