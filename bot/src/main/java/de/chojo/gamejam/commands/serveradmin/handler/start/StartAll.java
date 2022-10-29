package de.chojo.gamejam.commands.serveradmin.handler.start;

import de.chojo.gamejam.data.access.Guilds;
import de.chojo.gamejam.server.ServerService;
import de.chojo.gamejam.server.TeamServer;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class StartAll implements SlashHandler {
    private final ServerService serverService;
    private final Guilds guilds;

    public StartAll(ServerService serverService, Guilds guilds) {
        this.serverService = serverService;
        this.guilds = guilds;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var currentJam = guilds.guild(event).jams().getCurrentJam();
        if (currentJam.isEmpty()) {
            event.reply(context.localize("error.noactivejam")).queue();
            return;
        }
        var jam = currentJam.get();

        long count = jam.teams().teams().stream()
                        .map(serverService::get)
                        .map(TeamServer::start)
                        .filter(v -> v)
                        .count();
        event.reply("Started " + count + " servers.").queue();
    }
}
