package de.chojo.gamejam.commands.server.system;

import de.chojo.gamejam.data.access.Guilds;
import de.chojo.gamejam.server.ServerService;
import de.chojo.jdautil.interactions.slash.structure.handler.SlashHandler;
import de.chojo.jdautil.wrapper.EventContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;

import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

public class Delete implements SlashHandler {
    private static final Logger log = getLogger(Delete.class);
    private final Guilds guilds;
    private final ServerService serverService;

    public Delete(Guilds guilds, ServerService serverService) {
        this.guilds = guilds;
        this.serverService = serverService;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event, EventContext context) {
        var optJam = guilds.guild(event).jams().activeJam();

        if (optJam.isEmpty()) {
            event.reply(context.localize("error.nojamactive")).setEphemeral(true).queue();
            return;
        }

        var jam = optJam.get();
        var optTeam = jam.teams().byMember(event.getUser());

        if (optJam.isEmpty()) {
            event.reply(context.localize("error.noteam")).setEphemeral(true).queue();
            return;
        }

        var team = optTeam.get();

        var teamServer = serverService.get(team);
        boolean deleted;
        try {
            deleted = teamServer.purge();
        } catch (IOException e) {
            log.error("Could not purge server", e);
            event.reply("Something went wrong during server deletion").queue();
            return;
        }

        if (deleted) {
            event.reply("Server was deleted successfully.").queue();
        } else {
            event.reply("Server is not set up.").queue();
        }
    }
}
