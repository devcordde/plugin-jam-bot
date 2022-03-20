package de.chojo.gamejam.commands.subcommands;

import de.chojo.gamejam.data.JamData;
import de.chojo.gamejam.util.Future;
import de.chojo.jdautil.wrapper.SlashCommandContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public record JamAdminChangeVotes(JamData jamData, boolean voting, String content) implements SubCommand {

    @Override
    public void execute(SlashCommandInteractionEvent event, SlashCommandContext context) {
        jamData.getActiveJam(event.getGuild()).thenAccept(jam -> {
            if (jam.isEmpty()) {
                event.reply("There is no active jam.").queue();
                return;
            }
            jam.get().state().voting(voting);
            jamData.updateJamState(jam.get());
            event.reply(content).queue();
        }).whenComplete(Future.error());
    }
}
